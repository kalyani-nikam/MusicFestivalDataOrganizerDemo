package com.music.festival.demo.service;

import com.music.festival.demo.model.RecordLabel;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;

import java.util.List;

/**
 * Service layer interface for music festival application.
 */
public interface MusicFestivalService {

    /**
     * Get a list of record labels with band and festival data.
     * @return a list of {@link RecordLabel}s
     * @throws ResponseParsingException
     */
    List<RecordLabel> getAllFestivals() throws ResponseParsingException;
}
