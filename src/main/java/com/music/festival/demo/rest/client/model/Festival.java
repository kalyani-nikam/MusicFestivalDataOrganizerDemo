package com.music.festival.demo.rest.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class Festival {

    private String name;
    private BandsList bands;
}
