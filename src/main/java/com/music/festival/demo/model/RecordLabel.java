package com.music.festival.demo.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter @Setter @RequiredArgsConstructor
public class RecordLabel {

    @NonNull
    private String name;
    private Map<String, Band> bands;
}
