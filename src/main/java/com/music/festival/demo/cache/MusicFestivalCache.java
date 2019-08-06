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

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

@Singleton
@Component
public class MusicFestivalCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicFestivalCache.class);

    @Autowired
    private MusicFestivalRESTApiClient apiClient;

    private Cache<String, RecordLabel> recordLabelCache;

    private LocalDateTime cachePopulationTimestamp;

    private static final Integer TIME_TO_LIVE_IN_HOURS = 24;

    @PostConstruct
    public void initCache() {
        recordLabelCache = CacheBuilder.newBuilder().build();
    }

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

    private void populateCache() throws ResponseParsingException {
        // API call to get festivals data
        List<Festival> festivals = apiClient.getFestivals();
        LOGGER.debug("Populating cache with music festivals data.");

        // Prepare cache and populate with restructured festival data
        recordLabelCache.putAll(restructureFestivalData(festivals));

        cachePopulationTimestamp = LocalDateTime.now();
    }

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
                            Map<String, com.music.festival.demo.model.Festival> festivalsByBand = createOrGetetFestivalsByBand(recordLabelBand);

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

    private LinkedHashMap<String, com.music.festival.demo.model.Festival> getSortedFestivals(com.music.festival.demo.model.Band band) {
        return band.getFestivals()
                .entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    private LinkedHashMap<String, com.music.festival.demo.model.Band> getSorterBands(RecordLabel recordLabel) {
        return recordLabel.getBands()
                .entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    private LinkedHashMap<String, RecordLabel> getSorterRecordLabels(Map<String, RecordLabel> recordLabelsMap) {
        return recordLabelsMap.entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

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

    private Map<String, com.music.festival.demo.model.Festival> createOrGetetFestivalsByBand(com.music.festival.demo.model.Band recordLabelBand) {
        Map<String, com.music.festival.demo.model.Festival> festivalsByBand = recordLabelBand.getFestivals();
        if(festivalsByBand == null) {
            festivalsByBand = new HashMap<String, com.music.festival.demo.model.Festival>();
            recordLabelBand.setFestivals(festivalsByBand);
        }
        return festivalsByBand;
    }

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
