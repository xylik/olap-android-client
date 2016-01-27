package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuDimension;

import java.util.List;

/**
 * Created by igor on 18/01/16.
 */
public class Dimension {
    private SaikuDimension data;
    private List<Hierarchy> hierarchies;

    public Dimension(SaikuDimension data, List<Hierarchy> hierarchies) {
        this.data = data;
        this.hierarchies = hierarchies;
    }

    public SaikuDimension getData() {
        return data;
    }

    public void setData(SaikuDimension data) {
        this.data = data;
    }

    public List<Hierarchy> getHierarchies() {
        return hierarchies;
    }

    public void setHierarchies(List<Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
    }
}
