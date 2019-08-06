package com.music.festival.demo.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Model class to represent a band. Contains a list of festivals attended.
 */
@Getter @Setter @RequiredArgsConstructor
public class Band {

    @NonNull
    private String name;
    private Map<String, Festival> festivals;
}
