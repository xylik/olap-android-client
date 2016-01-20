package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuDimension;

import java.util.List;

/**
 * Created by igor on 18/01/16.
 */
public class Dimension {
    private List<Level> levels;
    private SaikuDimension data;

    public Dimension(SaikuDimension data, List<Level> levels ) {
        this.data = data;
        this.levels = levels;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public SaikuDimension getData() {
        return data;
    }

    public void setData(SaikuDimension data) {
        this.data = data;
    }
}
