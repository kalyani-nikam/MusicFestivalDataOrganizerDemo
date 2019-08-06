package com.music.festival.demo.service.impl;

import com.music.festival.demo.cache.MusicFestivalCache;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class MusicFestivalServiceImplTest {
    @Mock
    MusicFestivalCache mockCache;

    @InjectMocks
    MusicFestivalServiceImpl musicFestivalService = new MusicFestivalServiceImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Verify interaction with the cache
     */
    @Test
    public void testGetAllFestivals() throws ResponseParsingException {
        Mockito.when(mockCache.getAllMusicFestivals()).thenReturn(new ArrayList<>());
        musicFestivalService.getAllFestivals();
        Mockito.verify(mockCache, Mockito.times(1)).getAllMusicFestivals();
    }
}
