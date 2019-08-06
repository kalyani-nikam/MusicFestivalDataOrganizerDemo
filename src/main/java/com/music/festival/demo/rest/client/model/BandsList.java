package com.music.festival.demo.rest.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter @Setter @NoArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class BandsList extends ArrayList<Band> {
    private List<Band> bands;

    public Iterator<Band> getElements() {
        return this.iterator();
    }
}
