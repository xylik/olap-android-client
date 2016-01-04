package com.fer.hr.olap.rest.dto.query2.queryModel;

import java.util.ArrayList;
import java.util.List;

public class ThinDetails {
	
	private ThinQueryModel.AxisLocation axis;
	private Location location = Location.BOTTOM;
	private List<ThinMeasure> measures = new ArrayList<>();
	
	public enum Location {
		TOP,
		BOTTOM
	}
	
	public ThinDetails() {}

	public ThinDetails(ThinQueryModel.AxisLocation axis, Location location, List<ThinMeasure> measures) {
		this.axis = axis;
		this.measures = measures;
		this.location = location;
	}

  /**
	 * @return the axis
	 */
	public ThinQueryModel.AxisLocation getAxis() {
		return axis;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @return the measures
	 */
	public List<ThinMeasure> getMeasures() {
		return measures;
	}

	
	
	
	
	

}
