package com.fer.hr.services.repository;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.fer.hr.App;
import com.fer.hr.model.CubeMeta;
import com.fer.hr.rest.dto.discover.SaikuCatalog;
import com.fer.hr.rest.dto.discover.SaikuConnection;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;
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
    private List<CubeMeta> cubesMetaData;

    @Override
    public void getAllCubesMeta(boolean refreshData, Callback<List<CubeMeta>> callback) {
        if(refreshData || cubesMetaData == null) new CubesMetaTask().execute(callback);
        else if (cubesMetaData != null) callback.success(cubesMetaData);
    }

    private class CubesMetaTask extends AsyncTask<Callback<List<CubeMeta>>, Void, List<CubeMeta>> {
        private Callback<List<CubeMeta>> callback;
        private Exception ex;

        @Override
        protected List<CubeMeta> doInBackground(Callback<List<CubeMeta>>... params) {
            callback = params[0];
            ArrayList<CubeMeta> cubesMeta = null;
            try {
                cubesMeta = getMetadata();
            }catch(RetrofitError r) {
                r.printStackTrace();
                //if network problem occured retry after 2 seconds
                if(r.getKind().equals(RetrofitError.Kind.NETWORK)) {
                    try {
                        Thread.sleep(2000);
                        cubesMeta = getMetadata();
                    } catch (InterruptedException | RetrofitError ex) {
                        ex.printStackTrace();
                        this.ex = ex;
                    }
                }
            }
            return cubesMeta;
        }

        @Override
        protected void onPostExecute(List<CubeMeta> cubesMeta) {
            if(callback != null) {
                if(cubesMeta != null) {
                    SaikuRestRepository.this.cubesMetaData = cubesMeta;
                    callback.success(cubesMeta);
                }
                else callback.failure(ex);
            }
        }
    }

    @NonNull
    private ArrayList<CubeMeta> getMetadata() {
        SaikuConnection[] connections = App.api.getConnectionsMetadata(user);

        List<SaikuCube> allCubes = new ArrayList<>();
        for (SaikuConnection conn : connections) {
            for (SaikuCatalog catalog : conn.getCatalogs()) {
                for (SaikuSchema schema : catalog.getSchemas()) {
                    for (SaikuCube cube : schema.getCubes()) {
                        allCubes.add(cube);
                    }
                }
            }
        }

        ArrayList<CubeMeta> cubesMeta = new ArrayList<>();
        for (SaikuCube c : allCubes) {
            SaikuCubeMetadata meta = App.api.getCubeMetadata(user, c.getConnection(), c.getCatalog(), c.getSchema(), c.getName());
            cubesMeta.add(new CubeMeta(c, meta));
        }
        return cubesMeta;
    }
}
