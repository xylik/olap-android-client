package com.fer.hr.services.repository;

import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuDimension;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;
import com.fer.hr.rest.dto.discover.SimpleCubeElement;
import com.fer.hr.rest.dto.query2.ThinQuery;
import com.fer.hr.rest.dto.queryResult.QueryResult;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.common.IService;

import java.util.List;

/**
 * Created by igor on 17/01/16.
 */
public interface IRepository extends IService {

    void getFreshCubesMeta(final Callback<List<CubeWithMetaData>> callback);

    List<CubeWithMetaData> getCubesMeta();

    List<SaikuCube> getCubesFromAllConnections();

    List<SaikuMeasure> getMeasuresForCube(SaikuCube cube);

    List<SaikuDimension> getDimensionsForCube(SaikuCube cube);

    List<SaikuLevel> getLevelsOfHierarchy(SaikuCube cube, String hierarchyUniqueName);

    void getMembersForLevel(SaikuCube cube, SaikuLevel level, final Callback<List<SimpleCubeElement>> callback);

    void executeThinQuery(ThinQuery query, Callback<QueryResult> callback);
}
