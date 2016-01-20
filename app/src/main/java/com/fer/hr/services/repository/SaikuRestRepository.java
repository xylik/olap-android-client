package com.fer.hr.services.repository;

import android.os.AsyncTask;

import com.fer.hr.App;
import com.fer.hr.model.CubeWithMetaData;
import com.fer.hr.rest.dto.discover.SaikuCatalog;
import com.fer.hr.rest.dto.discover.SaikuConnection;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;
import com.fer.hr.rest.dto.discover.SaikuDimension;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;
import com.fer.hr.rest.dto.discover.SaikuSchema;
import com.fer.hr.services.common.Callback;
import com.fer.hr.data.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by igor on 17/01/16.
 */
public class SaikuRestRepository implements IRepository {
    private String user = Constants.DEFAULT_USER;
    private List<CubeWithMetaData> cubesFromAllConnectionsWithMetaData;
    private List<SaikuCube> cubesFromAllConnections = new ArrayList<>();

    @Override
    public void getFreshCubesMeta(Callback<List<CubeWithMetaData>> callback) {
        new CubesMetaTask().execute(callback);
    }

    @Override
    public List<CubeWithMetaData> getCubesMeta() {
        if(cubesFromAllConnectionsWithMetaData != null) return cubesFromAllConnectionsWithMetaData;

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

        ArrayList<CubeWithMetaData> cubesMeta = new ArrayList<>();
        for (SaikuCube c : cubesFromAllConnections) {
            SaikuCubeMetadata meta = App.api.getCubeMetadata(user, c.getConnection(), c.getCatalog(), c.getSchema(), c.getName());
            cubesMeta.add(new CubeWithMetaData(c, meta));
        }
        return cubesMeta;
    }

    @Override
    public List<SaikuCube> getCubesFromAllConnections() {
        return cubesFromAllConnections;
    }

    @Override
    public List<SaikuMeasure> getMeasuresForCube(SaikuCube cube) {
        for(CubeWithMetaData metaCube: cubesFromAllConnectionsWithMetaData) {
            if(metaCube.getCube() == cube) return metaCube.getSaikuCubeMetadata().getMeasures();
        }
        return null;
    }

    @Override
    public List<SaikuDimension> getDimensionsForCube(SaikuCube cube) {
        for(CubeWithMetaData metaCube: cubesFromAllConnectionsWithMetaData) {
            if(metaCube.getCube() == cube) return metaCube.getSaikuCubeMetadata().getDimensions();
        }
        return null;
    }

    private class CubesMetaTask extends AsyncTask<Callback<List<CubeWithMetaData>>, Void, List<CubeWithMetaData>> {
        private Callback<List<CubeWithMetaData>> callback;
        private Exception ex;

        @Override
        protected List<CubeWithMetaData> doInBackground(Callback<List<CubeWithMetaData>>... params) {
            callback = params[0];
            ArrayList<CubeWithMetaData> cubesMeta = null;
            try {
                cubesMeta = (ArrayList)getCubesMeta();
            }catch(RetrofitError r) {
                r.printStackTrace();
                this.ex = r;
                //if network problem occured retry after 2 seconds
                if(r.getKind().equals(RetrofitError.Kind.NETWORK)) {
                    try {
                        Thread.sleep(2000);
                        cubesMeta = (ArrayList)getCubesMeta();
                    } catch (InterruptedException | RetrofitError ex) {
                        this.ex = ex;
                    }
                }
            }
            return cubesMeta;
        }

        @Override
        protected void onPostExecute(List<CubeWithMetaData> cubesMeta) {
            if(callback != null) {
                if(cubesMeta != null) {
                    SaikuRestRepository.this.cubesFromAllConnectionsWithMetaData = cubesMeta;
                    callback.success(cubesMeta);
                }
                else callback.failure(ex);
            }
        }
    }
}