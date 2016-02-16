package com.fer.hr.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by igor on 14/02/16.
 */
public class GraphData implements Serializable{
    List<List<String>> xLabels;
    List<List<String>> yLabels;
    List<List<Number>> yValues;

    public GraphData(List<List<String>> xLabels, List<List<String>> yLabels, List<List<Number>> yValues) {
        this.xLabels = xLabels;
        this.yLabels = yLabels;
        this.yValues = yValues;
    }

    public List<List<String>> getxLabels() {
        return xLabels;
    }

    public void setxLabels(List<List<String>> xLabels) {
        this.xLabels = xLabels;
    }

    public List<List<String>> getyLabels() {
        return yLabels;
    }

    public void setyLabels(List<List<String>> yLabels) {
        this.yLabels = yLabels;
    }

    public List<List<Number>> getyValues() {
        return yValues;
    }

    public void setyValues(List<List<Number>> yValues) {
        this.yValues = yValues;
    }
}
