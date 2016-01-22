package com.fer.hr.model;

import java.util.List;

/**
 * Created by igor on 21/01/16.
 */
public class SelectionGroup {
    private String caption;
    private List<SelectionEntity> entities;

    public SelectionGroup(String caption, List<SelectionEntity> entities) {
        this.caption = caption;
        this.entities = entities;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<SelectionEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<SelectionEntity> entities) {
        this.entities = entities;
    }
}
