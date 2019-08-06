package com.music.festival.demo.rest.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class to represent a music festival. Used at client for de-serializing API response.
 */
@Getter @Setter @NoArgsConstructor
public class Festival {

    private String name;
    private BandsList bands;
}
