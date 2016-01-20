package com.fer.hr.utils;

import com.fer.hr.model.Dimension;
import com.fer.hr.model.Level;
import com.fer.hr.rest.dto.discover.SaikuDimension;
import com.fer.hr.rest.dto.discover.SaikuHierarchy;
import com.fer.hr.rest.dto.discover.SaikuLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by igor on 18/01/16.
 */
public class CubeMetaConverterUtil {

    private CubeMetaConverterUtil(){}

    public static HashMap<Integer, Dimension> convertToNestedListFormat(List<SaikuDimension> dimensions) {
        HashMap<Integer, Dimension> result = new HashMap<>();

        int group = -1;
        for(SaikuDimension d: dimensions) {
            group++;
            int position = -1;
            ArrayList<Level> levels = new ArrayList<>();
            for(SaikuHierarchy h: d.getHierarchies()) {
                for(SaikuLevel l: h.getLevels()) {
                    position++;
                    levels.add(new Level(Level.State.NEUTRAL, l, position));
                }
            }
            result.put(group, new Dimension(d, levels));
        }
        return result;
    }
}
