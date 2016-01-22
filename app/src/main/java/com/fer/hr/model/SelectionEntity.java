package com.fer.hr.model;

/**
 * Created by igor on 21/01/16.
 */
public class SelectionEntity {
    private String uniqueName;
    private String caption;

    public SelectionEntity(String uniqueName, String caption) {
        this.uniqueName = uniqueName;
        this.caption = caption;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
