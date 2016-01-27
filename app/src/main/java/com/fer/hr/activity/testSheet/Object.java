package com.fer.hr.activity.testSheet;

import java.util.ArrayList;
import java.util.List;

public class Object {
    public String title; // use getters and setters instead
    public List<Object> children; // same as above

    public Object() {
        children = new ArrayList<Object>();
    }
}