package com.fer.hr.services.repository;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.fer.hr.App;
import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.rest.dto.discover.SaikuCatalog;
import com.fer.hr.rest.dto.discover.SaikuConnection;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;
import com.fer.hr.rest.dto.discover.SaikuDimension;
import com.fer.hr.rest.dto.discover.SaikuHierarchy;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;
import com.fer.hr.rest.dto.discover.SaikuSchema;
import com.fer.hr.rest.dto.discover.SimpleCubeElement;
import com.fer.hr.rest.dto.query2.ThinQuery;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.rest.dto.queryResult.QueryResult;
import com.fer.hr.services.common.Callback;
import com.fer.hr.data.Constants;
import com.fer.hr.services.common.ServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by igor on 17/01/16.
 */
public class SaikuRestRepository implements IRepository {
    private static SaikuRestRepository instance;
    private SimpleCacheManger cacheMng;

    private boolean isEmpty = true;
    private String user = Constants.DEFAULT_USER;
    private List<CubeWithMetaData> cubesFromAllConnectionsWithMetaData;
    private List<SaikuCube> cubesFromAllConnections = new ArrayList<>();

    private SaikuRestRepository() {
        this.cacheMng = SimpleCacheManger.instance();
    }

    public static SaikuRestRepository instance() {
        if(instance == null) instance = new SaikuRestRepository();

        return instance;
    }

    @Override
    public void getFreshCubesMeta(Callback<List<CubeWithMetaData>> callback) {
        new CubesMetaTask().execute(callback);
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public List<CubeWithMetaData> getCubesMeta() {
        if(cacheMng.getCubesMeta() != null) return cacheMng.getCubesMeta();
//        else if (cubesFromAllConnectionsWithMetaData != null) return cubesFromAllConnectionsWithMetaData;

        SaikuConnection[] connections = App.api.getConnectionsMetadata(user);
        cubesFromAllConnections = new ArrayList<>();
        for (SaikuConnection conn : connections) {
            for (SaikuCatalog catalog : conn.getCatalogs()) {
                for (SaikuSchema schema : catalog.getSchemas()) {
                    for (SaikuCube cube : schema.getCubes()) {
                        cubesFromAllConnections.add(cube);
                    }
                }
            }
        }
        cacheMng.setCubesFromAllConnections(cubesFromAllConnections);

        ArrayList<CubeWithMetaData> cubesMeta = new ArrayList<>();
        for (SaikuCube c : cubesFromAllConnections) {
            SaikuCubeMetadata meta = App.api.getCubeMetadata(user, c.getConnection(), c.getCatalog(), TextUtils.isEmpty(c.getSchema()) ? "null" : c.getSchema(), c.getName());
            cubesMeta.add(new CubeWithMetaData(c, meta));
        }
        isEmpty = false;
        return cubesMeta;
    }

    @Override
    public List<SaikuCube> getCubesFromAllConnections() {
        return cacheMng.getCubesFromAllConnections();
    }

    @Override
    public List<SaikuMeasure> getMeasuresForCube(SaikuCube cube) {
        for (CubeWithMetaData metaCube : cacheMng.getCubesMeta())
            if (metaCube.getCube().equals(cube)) return metaCube.getSaikuCubeMetadata().getMeasures();

        return null;
    }

    @Override
    public List<SaikuDimension> getDimensionsForCube(SaikuCube cube) {
        for (CubeWithMetaData metaCube : cacheMng.getCubesMeta()) {
            if (metaCube.getCube().equals(cube)) return metaCube.getSaikuCubeMetadata().getDimensions();
        }
        return null;
    }

    @Override
    public List<SaikuLevel> getLevelsOfHierarchy(SaikuCube cube, String hierarchyUniqueName) {
        List<SaikuDimension> cubeDimensions = getDimensionsForCube(cube);
        List<SaikuLevel> hierarchyLevels = new ArrayList<>();
        for(SaikuDimension d : cubeDimensions)
            for(SaikuHierarchy h : d.getHierarchies())
                if(h.getUniqueName().equals(hierarchyUniqueName))
                    return h.getLevels();
        return hierarchyLevels;
    }

    @Override
    public void getMembersForLevel(SaikuCube c, SaikuLevel l, Callback<List<SimpleCubeElement>> callback) {
        //"uniqueName": "[Customer].[Customers].[Country]"
        String[] parts = l.getUniqueName().split("[\\[\\].]+");
        if (parts.length != 4) {
            if (callback != null)
                callback.failure(new ServiceException("Level uniqueName split failed!"));
            return;
        }
        String dName = parts[1];
        String hName = parts[2];
        String lName = parts[3];
        App.api.getLevelMembers(
                user, c.getConnection(), c.getCatalog(), TextUtils.isEmpty(c.getSchema()) ? "null" : c.getSchema(), c.getName(), dName, hName, lName,

                new retrofit.Callback<List<SimpleCubeElement>>() {
                    @Override
                    public void success(List<SimpleCubeElement> simpleCubeElements, Response response) {
                        if (callback != null) callback.success(simpleCubeElements);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (callback != null) callback.failure(error);
                    }
                }
        );
    }

    @Override
    public void executeThinQuery(String mdx, SaikuCube cube, Callback<QueryResult> callback) {
        ThinQuery query = new ThinQuery(UUID.randomUUID().toString(), cube, mdx);

        QueryResult cachedResult = cacheMng.getResultForQuery(query.getMdx());
        if(cachedResult != null) {
            callback.success(cachedResult);
            return;
        }

        Map<String, Object> p = query.getProperties();
        p.put("saiku.olap.query.filter", true);
        p.put("saiku.olap.query.nonempty", true);
        p.put("saiku.olap.query.nonempty.columns", true);
        p.put("saiku.olap.query.nonempty.rows", true);
        p.put("saiku.olap.result.formatter", "flat");
        p.put("saiku.ui.render.mode", "table");

        App.api.executeThinQuery(query, new retrofit.Callback<QueryResult>() {
            @Override
            public void success(QueryResult queryResult, Response response) {
                if (queryResult.getCellset() != null) {
                    formatTable(queryResult);
                    cacheMng.addQueryResult(query.getMdx(), queryResult);
                }
                if (callback != null) callback.success(queryResult);
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) callback.failure(error);
            }
        });
    }

    private void formatTable(QueryResult table) {
        for(Cell[] row: table.getCellset()){
            for(Cell cell: row){
                String cellValue = cell.getValue();
                if(isCellEmpty(cellValue)) cell.setValue("-");
            }
        }
    }

    private boolean isCellEmpty(String cellValue) {
        return TextUtils.isEmpty(cellValue) || cellValue.equals("null");
    }

    private class CubesMetaTask extends AsyncTask<Callback<List<CubeWithMetaData>>, Void, List<CubeWithMetaData>> {
        private Callback<List<CubeWithMetaData>> callback;
        private Exception ex;

        @Override
        protected List<CubeWithMetaData> doInBackground(Callback<List<CubeWithMetaData>>... params) {
            callback = params[0];
            ArrayList<CubeWithMetaData> cubesMeta = null;
            try {
                cubesMeta = (ArrayList) getCubesMeta();
            } catch (RetrofitError r) {
                r.printStackTrace();
                this.ex = r;
                //if network problem occured retry after 2 seconds
                if (r.getKind().equals(RetrofitError.Kind.NETWORK)) {
                    try {
                        Thread.sleep(2000);
                        cubesMeta = (ArrayList) getCubesMeta();
                    } catch (InterruptedException | RetrofitError ex) {
                        this.ex = ex;
                    }
                }
            }
            return cubesMeta;
        }

        @Override
        protected void onPostExecute(List<CubeWithMetaData> cubesMeta) {
            if (callback != null) {
                if (cubesMeta != null) {
                    cacheMng.setCubesMeta(cubesMeta);
                    SaikuRestRepository.this.cubesFromAllConnectionsWithMetaData = cubesMeta;
                    callback.success(cubesMeta);
                } else callback.failure(ex);
            }
        }
    }
}