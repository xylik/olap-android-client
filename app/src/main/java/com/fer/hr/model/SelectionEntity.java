package com.fer.hr.model;

/**
 * Created by igor on 21/01/16.
 */
public class SelectionEntity {
    private String levelUniqueName;
    private String uniqueName;
    private String caption;

    public SelectionEntity(String levelUniqueName, String uniqueName, String caption) {
        this.levelUniqueName = levelUniqueName;
        this.uniqueName = uniqueName;
        this.caption = caption;
    }

    public SelectionEntity(String uniqueName, String caption) {
        this(null, uniqueName, caption);
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

    public String getLevelUniqueName() {
        return levelUniqueName;
    }

    public void setLevelUniqueName(String levelUniqueName) {
        this.levelUniqueName = levelUniqueName;
    }
}
