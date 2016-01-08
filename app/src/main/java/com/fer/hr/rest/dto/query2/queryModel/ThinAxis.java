package com.fer.hr.rest.dto.query2.queryModel;

//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fer.hr.rest.dto.query2.common.AbstractThinSortableQuerySet;
import com.fer.hr.rest.dto.query2.util.NamedList;
import com.fer.hr.rest.dto.query2.util.NamedListImpl;

import java.util.ArrayList;
import java.util.List;

//@JsonIgnoreProperties
public class ThinAxis extends AbstractThinSortableQuerySet {

    private transient ThinQueryModel.AxisLocation location;
    private List<ThinHierarchy> hierarchies = new NamedListImpl<>();
    private boolean nonEmpty;
    private List<String> aggs = new ArrayList<>();


    public ThinAxis() {
    }

    public ThinAxis(ThinQueryModel.AxisLocation location, NamedList<ThinHierarchy> hierarchies, boolean nonEmpty, List<String> aggs) {
        this.location = location;
        if (hierarchies != null) {
            this.hierarchies = hierarchies;
        }
        if (aggs != null) {
            this.aggs = aggs;
        }
        this.nonEmpty = nonEmpty;
    }

    //	@JsonIgnore
    @Override
    public String getName() {
        return location.toString();
    }

    /**
     * @return the location
     */
    public ThinQueryModel.AxisLocation getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(ThinQueryModel.AxisLocation location) {
        this.location = location;
    }

    /**
     * @return the hierarchies
     */
    public List<ThinHierarchy> getHierarchies() {
        return hierarchies;
    }

    public ThinHierarchy getHierarchy(String name) {
        return ((NamedListImpl<ThinHierarchy>) hierarchies).get(name);
    }

    /**
     * @return the nonEmpty
     */
    public boolean isNonEmpty() {
        return nonEmpty;
    }

    /**
     * @param nonEmpty the nonEmpty to set
     */
    public void setNonEmpty(boolean nonEmpty) {
        this.nonEmpty = nonEmpty;
    }

    public List<String> getAggregators() {
        return aggs;
    }

}
