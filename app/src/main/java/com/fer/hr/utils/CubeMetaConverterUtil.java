package com.fer.hr.utils;

import com.fer.hr.model.Dimension;
import com.fer.hr.model.Hierarchy;
import com.fer.hr.model.Level;
import com.fer.hr.model.SelectionGroup;
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

            ArrayList<Hierarchy> hierarchies = new ArrayList<>();
            for(SaikuHierarchy h: d.getHierarchies()) {

                int position = -1;
                ArrayList<Level> levels = new ArrayList<>();
                for(SaikuLevel l: h.getLevels()) {
                    position++;
                    levels.add(new Level(Level.State.NEUTRAL, l, position));
                }

                hierarchies.add(new Hierarchy(h, levels));
            }
            result.put(group, new Dimension(d, hierarchies));
        }
        return result;
    }

    public static HashMap<Integer, SelectionGroup> getEmptySelectionGroup() {
        HashMap<Integer, SelectionGroup> selectionGroups = new HashMap<>();
        SelectionGroup measureGroup = new SelectionGroup("Measures on Collumns", new ArrayList<>());
        SelectionGroup collGroup = new SelectionGroup("Collumns", new ArrayList<>());
        SelectionGroup rowGroup = new SelectionGroup("Rows", new ArrayList<>());
        SelectionGroup filterGroup = new SelectionGroup("Filters", new ArrayList<>());

        selectionGroups.put(0, measureGroup);
        selectionGroups.put(1, collGroup);
        selectionGroups.put(2, rowGroup);
        selectionGroups.put(3, filterGroup);

        return selectionGroups;
    }
}
