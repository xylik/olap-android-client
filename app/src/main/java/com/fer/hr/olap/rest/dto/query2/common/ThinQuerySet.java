package com.fer.hr.olap.rest.dto.query2.common;

import com.fer.hr.olap.rest.dto.query2.filter.ThinFilter;

import java.util.List;


public interface ThinQuerySet {

		String getName();
		
		void setMdx(String mdxSetExpression);
		
		String getMdx();
		
		void addFilter(ThinFilter filter);
		
		void setFilter(int index, ThinFilter filter);
		
		List<ThinFilter> getFilters();
		
		void clearFilters();
}
