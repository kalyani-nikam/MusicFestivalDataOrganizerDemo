package com.music.festival.demo;

import com.music.festival.demo.model.RecordLabel;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.service.MusicFestivalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring boot application class.
 * Exposes method to list festivals. The method is on post construct  by default.
 * The boolean system property 'listFestivalDataOnAppStart' can be set to false to change this behavior.
 */
@SpringBootApplication
public class MusicFestivalDataOrganizerDemo {

    @Autowired
    private MusicFestivalService musicFestivalService;

    /**
     * System property listFestivalsOnApplicationStart is set to TRUE by default.
     */
    @Value("${listFestivalsOnApplicationStart:true}")
    private Boolean listFestivalDataOnAppStart;

    /**
     * System property outputFileUri is set to RestructuredFestivalData.txt by default.
     */
    @Value("${outputFileUri:RestructuredFestivalData.txt}")
    private String outputFilePath = "RestructuredFestivalData.txt";

    private static final String LEADING_SPACES = "     ";

    /**
     * This method is invoked post construct.
     * Writes festival data to the output file if 'listFestivalsOnApplicationStart' is TRUE.
     * @throws IOException
     * @throws ResponseParsingException
     */
    @PostConstruct
    public void listFestivals() throws IOException, ResponseParsingException {
        if(listFestivalDataOnAppStart) {
            List<RecordLabel> festivals = musicFestivalService.getAllFestivals();
            writeFestivalDataToFile(festivals, outputFilePath);
        }
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MusicFestivalDataOrganizerDemo.class, args);
    }

    /**
     * Helper method to write data to file.
     * @param recordLabels
     * @param outputFilePath
     * @throws IOException
     */
    private void writeFestivalDataToFile(List<RecordLabel> recordLabels, String outputFilePath) throws IOException {
        List<String> lines = getOutputFileContent(recordLabels);
        Files.write(Paths.get(this.outputFilePath), lines);
    }

    /**
     * Helper method to organize contents to be written to the output file.
     * @param recordLabels
     * @return
     */
    private List<String> getOutputFileContent(List<RecordLabel> recordLabels) {
        List<String> lines = new ArrayList<>();
        recordLabels.forEach(recordLabel -> {
            // Add record label name
            lines.add(getFormattedRecordLabel(recordLabel.getName()));

            // Add Band Names
            recordLabel.getBands().forEach((bandName, band) -> {
                lines.add(getFormattedBandName(bandName));

                // Add Festivals
                band.getFestivals().forEach((festivalName, festival) -> {
                    lines.add(getFormattedFestivalName(festivalName));
                });
            });
        });

        return lines;
    }

    private String getFormattedFestivalName(String festivalName) {
        return LEADING_SPACES + LEADING_SPACES + festivalName;
    }

    private String getFormattedBandName(String bandName) {
        return LEADING_SPACES + bandName;
    }

    private String getFormattedRecordLabel(String recordLabelName) {
        return recordLabelName;
    }
}
