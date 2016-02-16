package com.fer.hr.services.repository;

import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.queryResult.QueryResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by igor on 31/01/16.
 */
public class SimpleCacheManger implements Serializable {
    private static SimpleCacheManger instance;

    private List<CubeWithMetaData> cubesFromAllConnectionsWithMetaData;
    private List<SaikuCube> cubesFromAllConnections;
    private HashMap<String, QueryResult> queryResults;

    private SimpleCacheManger(){}

    public static synchronized SimpleCacheManger instance() {
        if(instance == null) instance = new SimpleCacheManger();

        return instance;
    }

    public List<CubeWithMetaData> getCubesMeta() {
        return cubesFromAllConnectionsWithMetaData;
    }

    public void setCubesMeta(List<CubeWithMetaData> cubesMeta) {
        this.cubesFromAllConnectionsWithMetaData = cubesMeta;
    }

    public List<SaikuCube> getCubesFromAllConnections() {
        return cubesFromAllConnections;
    }

    public void setCubesFromAllConnections(List<SaikuCube> cubes) {
        cubesFromAllConnections = cubes;
    }

    QueryResult getResultForQuery(String mdx){
        return queryResults != null ? queryResults.get(mdx) : null;
    }

    public void addQueryResult(String mdx, QueryResult result) {
        if(queryResults == null) queryResults = new HashMap<>();
        queryResults.put(mdx, result);
    }
}
