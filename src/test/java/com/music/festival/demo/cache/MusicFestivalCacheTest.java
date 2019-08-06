package com.music.festival.demo.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.music.festival.demo.model.Band;
import com.music.festival.demo.model.RecordLabel;
import com.music.festival.demo.rest.client.MusicFestivalRESTApiClient;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.rest.client.model.Festival;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CacheBuilder.class })
public class MusicFestivalCacheTest {

    @Mock
    MusicFestivalRESTApiClient mockApiClient;

    @InjectMocks
    MusicFestivalCache cache = MusicFestivalCache.getInstance();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllMusicFestivals() throws ResponseParsingException, IOException {
        when(mockApiClient.getFestivals()).thenReturn(getDummyFestivalsList());

        ArgumentCaptor<Map<String, RecordLabel>> captor = ArgumentCaptor.forClass(Map.class);

        List<RecordLabel> actualSortedRecordLabelList = cache.getAllMusicFestivals();

        // Verify interaction with the api client
        verify(mockApiClient, times(1)).getFestivals();

        String[] expectedRecordLabels = getExpectedRecordLabels();

        assertNotNull(actualSortedRecordLabelList);
        assertEquals(expectedRecordLabels.length, actualSortedRecordLabelList.size());

        // Verify that record labels are sorted alphabetically
        List<String> actualSortedRecordLabelNames = actualSortedRecordLabelList.stream().map(recordLabel -> recordLabel.getName()).collect(Collectors.toList());
        assertThat("Record labels are not sorted in ascending order", actualSortedRecordLabelNames, Matchers.contains(expectedRecordLabels));

        // Verify one record label from the map
        RecordLabel recordLabelToVefiry = actualSortedRecordLabelList.get(3);
        assertNotNull(recordLabelToVefiry.getBands());
        assertEquals("Fourth Woman Records", recordLabelToVefiry.getName());
        assertEquals(2, recordLabelToVefiry.getBands().size());
        assertThat(recordLabelToVefiry.getBands().keySet(), Matchers.contains("Jill Black", "The Black Dashes"));

        // Verify bands under the record label
        Band bandToVerify = recordLabelToVefiry.getBands().get("Jill Black");
        assertNotNull(bandToVerify);
        assertNotNull(bandToVerify.getFestivals());

        // Verify festivals under the band
        assertEquals(1, bandToVerify.getFestivals().size());
        assertThat(bandToVerify.getFestivals().keySet(), Matchers.contains("LOL-palooza"));
    }

    private List<Festival> getDummyFestivalsList() throws IOException {
        String festivalsString = "[{\"name\":\"LOL-palooza\",\"bands\":[{\"name\":\"Werewolf Weekday\",\"recordLabel\":\"XS Recordings\"},{\"name\":\"Jill Black\",\"recordLabel\":\"Fourth Woman Records\"},{\"name\":\"Frank Jupiter\",\"recordLabel\":\"Pacific Records\"},{\"name\":\"Winter Primates\",\"recordLabel\":\"\"}]},{\"name\":\"Small Night In\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\",\"recordLabel\":\"Outerscope\"},{\"name\":\"Green Mild Cold Capsicum\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Yanke East\",\"recordLabel\":\"MEDIOCRE Music\"},{\"name\":\"The Black Dashes\",\"recordLabel\":\"Fourth Woman Records\"}]},{\"name\":\"Trainerella\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Still Bottom Records\"},{\"name\":\"YOUKRANE\",\"recordLabel\":\"Anti Records\"},{\"name\":\"Adrian Venti\",\"recordLabel\":\"Monocracy Records\"},{\"name\":\"Manish Ditch\",\"recordLabel\":\"ACR\"}]},{\"name\":\"Twisted Tour\",\"bands\":[{\"name\":\"Auditones\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\"},{\"name\":\"Summon\",\"recordLabel\":\"Outerscope\"}]},{\"bands\":[{\"name\":\"Critter Girls\",\"recordLabel\":\"ACR\"},{\"name\":\"Propeller\",\"recordLabel\":\"Pacific Records\"}]}]";;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(festivalsString, new TypeReference<List<Festival>>(){});
    }

    private String[] getExpectedRecordLabels() {
        return new String[]{"", "ACR", "Anti Records", "Fourth Woman Records", "MEDIOCRE Music", "Marner Sis. Recording", "Monocracy Records",
                "Outerscope", "Pacific Records", "Still Bottom Records", "XS Recordings"};
    }
}
