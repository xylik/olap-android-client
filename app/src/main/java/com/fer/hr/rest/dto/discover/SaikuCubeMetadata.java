package com.fer.hr.rest.dto.discover;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class SaikuCubeMetadata implements Serializable {

    private final List<SaikuDimension> dimensions;
    private final List<SaikuMeasure> measures;
    private final Map<String, Object> properties;


    public SaikuCubeMetadata(List<SaikuDimension> dimensions, List<SaikuMeasure> measures, Map<String, Object> properties) {
        this.dimensions = dimensions;
        this.measures = measures;
        this.properties = properties;
    }

    /**
     * @return the dimensions
     */
    public List<SaikuDimension> getDimensions() {
        return dimensions;
    }

    /**
     * @return the measures
     */
    public List<SaikuMeasure> getMeasures() {
        return measures;
    }


    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
}
