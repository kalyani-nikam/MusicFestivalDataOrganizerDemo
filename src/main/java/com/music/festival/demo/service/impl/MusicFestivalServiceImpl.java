package com.music.festival.demo.service.impl;

import com.music.festival.demo.cache.MusicFestivalCache;
import com.music.festival.demo.model.RecordLabel;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.service.MusicFestivalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MusicFestivalServiceImpl implements MusicFestivalService {

    @Autowired
    private MusicFestivalCache musicFestivalCache;

    @Override
    public List<RecordLabel> getAllFestivals() throws ResponseParsingException {
        List<RecordLabel> returnList = new ArrayList<>();
        return musicFestivalCache.getAllMusicFestivals();
    }
}
