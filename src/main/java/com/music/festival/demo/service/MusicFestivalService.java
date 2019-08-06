package com.music.festival.demo.service;

import com.music.festival.demo.model.RecordLabel;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;

import java.util.List;

public interface MusicFestivalService {

    List<RecordLabel> getAllFestivals() throws ResponseParsingException;
}
