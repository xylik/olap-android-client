package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuHierarchy;

import java.util.List;

/**
 * Created by igor on 25/01/16.
 */
public class Hierarchy {
    private SaikuHierarchy data;
    private List<Level> levels;

    public Hierarchy(SaikuHierarchy data, List<Level> levels) {
        this.levels = levels;
        this.data = data;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public SaikuHierarchy getData() {
        return data;
    }

    public void setData(SaikuHierarchy data) {
        this.data = data;
    }
}
