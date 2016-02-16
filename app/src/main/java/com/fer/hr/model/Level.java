package com.fer.hr.model;

import com.fer.hr.rest.dto.discover.SaikuLevel;

import java.io.Serializable;

/**
 * Created by igor on 18/01/16.
 */
public class Level implements Serializable {
    public static enum State {NEUTRAL, ROWS, COLLUMNS, FILTER};
    private State state;
    private int hierarchyPosition;
    private SaikuLevel data;

    public Level(State state, SaikuLevel data, int hierarchyPosition) {
        this.state = state;
        this.data = data;
        this.hierarchyPosition = hierarchyPosition;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public SaikuLevel getData() {
        return data;
    }

    public void setData(SaikuLevel data) {
        this.data = data;
    }

    public int getHierarchyPosition() {
        return hierarchyPosition;
    }

    public void setHierarchyPosition(int hierarchyPosition) {
        this.hierarchyPosition = hierarchyPosition;
    }
}
