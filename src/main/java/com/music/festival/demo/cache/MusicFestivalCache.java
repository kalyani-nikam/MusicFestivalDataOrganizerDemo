package com.music.festival.demo.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.music.festival.demo.model.RecordLabel;
import com.music.festival.demo.rest.client.MusicFestivalRESTApiClient;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.rest.client.model.Band;
import com.music.festival.demo.rest.client.model.Festival;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Singleton class that acts as cache for festival data.
 * The first invocation of getAllMusicFestivals() triggers REST API call to get festival data.
 * The data is cached for 24 hours after which it becomes stale.
 * Cache is reloaded on subsequent call to getAllMusicFestivals() after TTL has expired.
 */
@Component
public class MusicFestivalCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicFestivalCache.class);

    private static MusicFestivalCache cache = new MusicFestivalCache();

    @Autowired
    private MusicFestivalRESTApiClient apiClient;

    private Cache<String, RecordLabel> recordLabelCache;

    /**
     * Population time and time to live is required to keep track of stale cache
     */
    private LocalDateTime cachePopulationTimestamp;
    private static final Integer TIME_TO_LIVE_IN_HOURS = 24;

    /**
     * Private constructor for the singleton class.
     */
    private MusicFestivalCache() {
        recordLabelCache = CacheBuilder.newBuilder().build();
    }

    /**
     * Returns instance of MusicFestivalCache
     * @return instance of MusicFestivalCache
     */
    public static MusicFestivalCache getInstance() {
        return cache;
    }

    /**
     * Get restructured festival data
     * @return List of {@link RecordLabel}s
     * @throws ResponseParsingException when response string from REST API cannot be parsed
     */
    public List<RecordLabel> getAllMusicFestivals() throws ResponseParsingException {
        // Initialize cache if empty
        if(isCacheEmptyOrStale()) {
            populateCache();
        }

        // Prepare a list of record labels
        List<RecordLabel> recordLabelsList = recordLabelCache.asMap().values()
                .stream()
                .collect(toList());

        // Sort record labels alphabetically by name
        recordLabelsList.sort(Comparator.comparing(previous -> previous.getName()));

        return recordLabelsList;
    }

    /**
     * Invoke REST API to get festivals data.
     * Then, restructure the data and populate cache.
     * @throws ResponseParsingException
     */
    private void populateCache() throws ResponseParsingException {
        // API call to get festivals data
        List<Festival> festivals = apiClient.getFestivals();
        LOGGER.debug("Populating cache with music festivals data.");

        // Prepare cache and populate with restructured festival data
        recordLabelCache.putAll(restructureFestivalData(festivals));

        cachePopulationTimestamp = LocalDateTime.now();
    }

    /**
     * True  if cache is empty or stale.
     * @return
     */
    private boolean isCacheEmptyOrStale() {
        // Return true for cache is empty
        if(recordLabelCache.asMap() == null || recordLabelCache.asMap().isEmpty()) {
            return true;
        }

        // Return true for cache is stale
        LocalDateTime now = LocalDateTime.now();
        if(now.minusHours(TIME_TO_LIVE_IN_HOURS).isAfter(cachePopulationTimestamp)) {
            return true;
        }

        return false;
    }

    /**
     * Restructures the festivals data.
     * @param festivals
     * @return Map of {@link RecordLabel}s by name
     */
    private Map<String, RecordLabel> restructureFestivalData(List<Festival> festivals) {
        LOGGER.debug("Restructuring music festivals data.");
        final Map<String, RecordLabel> recordLabelsMap = new HashMap<String, RecordLabel>();

        if(festivals !=  null && festivals.size() > 0) {
            // Process each festival object to restructure data
            festivals.forEach(festival -> {
                        final String festivalName = Objects.toString(festival.getName(), "");
                        List<Band> bands = festival.getBands();

                        bands.forEach(band -> {
                            String bandName = Objects.toString(band.getName(), "");
                            String recordLabelName = Objects.toString(band.getRecordLabel(), "");

                            // Get and existing record label from map or create new.
                            RecordLabel recordLabel = createOrGetRecordLabelFromMap(recordLabelsMap, recordLabelName);

                            // Get band form the record label if its already been added, else create new
                            com.music.festival.demo.model.Band recordLabelBand = createOrGetBandByNameFromRecordLabel(bandName, recordLabel);

                            // Get festivals by the band and record label, create map if not already present
                            Map<String, com.music.festival.demo.model.Festival> festivalsByBand = createOrGetFestivalsByBand(recordLabelBand);

                            // Add festival to the band with bandName under the record label with recordLabelName
                            festivalsByBand.put(festivalName, new com.music.festival.demo.model.Festival(festivalName));

                            LOGGER.debug("Added festival " + festivalName + " to the band " + bandName + " for record label " + recordLabelName);
                        });
                    });
        }

        // Sort Band Names for each record label
        recordLabelsMap.forEach((recordLabelName, recordLabel) -> {
            LinkedHashMap<String, com.music.festival.demo.model.Band> sortedBandsMap = getSorterBands(recordLabel);
            recordLabel.setBands(sortedBandsMap);
            
            // Sort Festival Names for each band
            sortedBandsMap.forEach((bandName, band) -> {
                band.setFestivals(getSortedFestivals(band));
            });
        });


        return recordLabelsMap;
    }

    /**
     * Sorts festivals under this band in alphabetically ascending order by festival name
     * @param band
     * @return
     */
    private LinkedHashMap<String, com.music.festival.demo.model.Festival> getSortedFestivals(com.music.festival.demo.model.Band band) {
        return band.getFestivals()
                .entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Sorts bands under this record label in alphabetically ascending order by band name
     * @param recordLabel
     * @return
     */
    private LinkedHashMap<String, com.music.festival.demo.model.Band> getSorterBands(RecordLabel recordLabel) {
        return recordLabel.getBands()
                .entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Sorts record labels in alphabetically ascending order by record label name
     * @param recordLabelsMap
     * @return
     */
    private LinkedHashMap<String, RecordLabel> getSorterRecordLabels(Map<String, RecordLabel> recordLabelsMap) {
        return recordLabelsMap.entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    /**
     * Returns a record label from the given map. Creates a new map entry if it does not exist.
     * @param recordLabelsMap
     * @param recordLabelName
     * @return
     */
    private RecordLabel createOrGetRecordLabelFromMap(Map<String, RecordLabel> recordLabelsMap, String recordLabelName) {
        RecordLabel recordLabel = new RecordLabel(recordLabelName);
        if(recordLabelsMap.containsKey(recordLabelName)) {
            recordLabel = recordLabelsMap.get(recordLabelName);
        } else {
            recordLabelsMap.put(recordLabelName, recordLabel);
            LOGGER.debug("Creating a new record label entry in map for " + recordLabelName);
        }
        return recordLabel;
    }

    /**
     * Returns festivals under this band. Creates new map if it does not exist.
     * @param recordLabelBand
     * @return
     */
    private Map<String, com.music.festival.demo.model.Festival> createOrGetFestivalsByBand(com.music.festival.demo.model.Band recordLabelBand) {
        Map<String, com.music.festival.demo.model.Festival> festivalsByBand = recordLabelBand.getFestivals();
        if(festivalsByBand == null) {
            festivalsByBand = new HashMap<String, com.music.festival.demo.model.Festival>();
            recordLabelBand.setFestivals(festivalsByBand);
        }
        return festivalsByBand;
    }

    /**
     * Returns band by given band name from the record label. Creates one if it does not exist.
     * @param bandName
     * @param recordLabel
     * @return
     */
    private com.music.festival.demo.model.Band createOrGetBandByNameFromRecordLabel(String bandName, RecordLabel recordLabel) {
        Map<String, com.music.festival.demo.model.Band> recordLabelBands = recordLabel.getBands();
        if(recordLabelBands == null) {
            recordLabelBands = new HashMap<String, com.music.festival.demo.model.Band>();
            recordLabel.setBands(recordLabelBands);
        }
        com.music.festival.demo.model.Band recordLabelBand = new com.music.festival.demo.model.Band(bandName);
        if(recordLabelBands.containsKey(bandName)) {
            recordLabelBand = recordLabelBands.get(bandName);
        } else {
            recordLabelBands.put(bandName, recordLabelBand);
            LOGGER.debug("Added band " + bandName + " to record label " + recordLabel.getName());
        }
        return recordLabelBand;
    }

}
