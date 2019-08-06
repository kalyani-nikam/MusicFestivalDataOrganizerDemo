package com.music.festival.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.music.festival.demo.rest.client.MusicFestivalRESTApiClient;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.rest.client.model.Festival;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "listFestivalsOnApplicationStart=false")
public class MusicFestivalDataOrganizerDemoTest {

    /**
     * Mock only the API Client.
     * All the other beans will still be created by spring.
     */
    @MockBean
    MusicFestivalRESTApiClient mockApiClient;

    @Autowired
    MusicFestivalDataOrganizerDemo demoApp;

    private static final String TEST_OUTPUT_FILE_PATH = "TEST-RestructuredFestivalData.txt";
    private static final String VALID_TEST_FILE_PATH = Resources.getResource("Expected_Restructured_Festival_Data.txt").getPath();

    @Before
    public void setUp() throws ResponseParsingException, IOException {
        Mockito.when(mockApiClient.getFestivals()).thenReturn(getDummyFestivalsList());
    }

    @Test
    public void testListFestivals() throws IOException, ResponseParsingException {
        ReflectionTestUtils.setField(demoApp, "listFestivalDataOnAppStart", Boolean.TRUE);
        ReflectionTestUtils.setField(demoApp, "outputFilePath", TEST_OUTPUT_FILE_PATH);

        // Call listFestivals() to create the output file with festival data.
        demoApp.listFestivals();

        // Assert that output file exists
        Path outputFilePath = Paths.get(TEST_OUTPUT_FILE_PATH);
        assertTrue("Output file does not exist", Files.exists(outputFilePath));

        // Verify contents of the file

        List<String> linesToVerify = Files.readAllLines(outputFilePath);
        List<String> linesExpected = Files.readAllLines(Paths.get(VALID_TEST_FILE_PATH));

        assertNotNull(linesToVerify);
        assertEquals("The output file contains incorrect number of lines.", linesExpected.size(), linesToVerify.size());

        // Verify each line
        assertThat("One or more lines in the output file mismatch with the expected.", linesToVerify, Matchers.equalTo(linesExpected));
    }

    private List<Festival> getDummyFestivalsList() throws IOException {
        String festivalsString = "[{\"name\":\"LOL-palooza\",\"bands\":[{\"name\":\"Werewolf Weekday\",\"recordLabel\":\"XS Recordings\"},{\"name\":\"Jill Black\",\"recordLabel\":\"Fourth Woman Records\"},{\"name\":\"Frank Jupiter\",\"recordLabel\":\"Pacific Records\"},{\"name\":\"Winter Primates\",\"recordLabel\":\"\"}]},{\"name\":\"Small Night In\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\",\"recordLabel\":\"Outerscope\"},{\"name\":\"Green Mild Cold Capsicum\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Yanke East\",\"recordLabel\":\"MEDIOCRE Music\"},{\"name\":\"The Black Dashes\",\"recordLabel\":\"Fourth Woman Records\"}]},{\"name\":\"Trainerella\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Still Bottom Records\"},{\"name\":\"YOUKRANE\",\"recordLabel\":\"Anti Records\"},{\"name\":\"Adrian Venti\",\"recordLabel\":\"Monocracy Records\"},{\"name\":\"Manish Ditch\",\"recordLabel\":\"ACR\"}]},{\"name\":\"Twisted Tour\",\"bands\":[{\"name\":\"Auditones\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\"},{\"name\":\"Summon\",\"recordLabel\":\"Outerscope\"}]},{\"bands\":[{\"name\":\"Critter Girls\",\"recordLabel\":\"ACR\"},{\"name\":\"Propeller\",\"recordLabel\":\"Pacific Records\"}]}]";;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(festivalsString, new TypeReference<List<Festival>>(){});
    }
}
