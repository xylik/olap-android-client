package com.fer.hr.rest.dto.discover;

public class SimpleCubeElement extends AbstractSaikuObject {
    private String caption;

    public SimpleCubeElement() {
    }

    public SimpleCubeElement(String name, String uniqueName, String caption) {
        super(uniqueName, name);
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

}