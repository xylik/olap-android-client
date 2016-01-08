package com.fer.hr.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.fer.hr.R;
import com.google.gson.Gson;

import java.util.UUID;

import com.fer.hr.rest.api.SaikuApi;
import com.fer.hr.rest.dto.license.License;
import com.fer.hr.rest.dto.discover.SaikuConnection;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;
import com.fer.hr.rest.dto.queryResult.QueryResult;
import com.fer.hr.rest.dto.session.Session;
import com.fer.hr.rest.dto.query2.ThinQuery;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ActivityMain extends AppCompatActivity {
    public static SaikuApi api;
    public static SaikuCube cube;
    public static ThinQuery query;

    public static String mdx = "SELECT NON EMPTY {[Location].[Place name].Members} ON COLUMNS, NON EMPTY {[Product].[Product level].Members} ON ROWS FROM [Sales cube]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        api.createSession("admin", "admin", "en", new ResponseImp());
//        api.getConnectionsMetadata("admin", System.currentTimeMillis(), connectionsMetaCbk);
    }

    class ResponseImp extends ResponseCallback {
        @Override
        public void success(Response response) {
            api.getSession(System.currentTimeMillis(), sessionMetaCbk);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    }

    final Callback<Session> sessionMetaCbk = new Callback<Session>() {
        @Override
        public void success(Session session, Response response) {
            api.getLicense(System.currentTimeMillis(), licenseMetaCbk);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    };

    final Callback<License> licenseMetaCbk = new Callback<License>() {
        @Override
        public void success(License license, Response response) {
            api.getConnectionsMetadata("admin", System.currentTimeMillis(), connectionsMetaCbk);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    };

    final Callback<SaikuConnection[]> connectionsMetaCbk = new Callback<SaikuConnection[]>() {
        @Override
        public void success(SaikuConnection[] saikuConnections, Response response) {
            SaikuConnection sc = saikuConnections[0];
            String connection = sc.getName();
            String catalog = sc.getCatalogs().get(0).getName();
            String schema = sc.getCatalogs().get(0).getSchemas().get(0).getName();
            cube = sc.getCatalogs().get(0).getSchemas().get(0).getCubes().get(0);
            String cube = ActivityMain.cube.getName();
            String key = connection + "/" + catalog + "/" + schema + "/" + cube;
            api.getCubeMetadata("admin", connection, catalog, schema, cube, key, System.currentTimeMillis(), cubeMetaCbk);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    };

    final Callback<SaikuCubeMetadata> cubeMetaCbk = new Callback<SaikuCubeMetadata>() {
        @Override
        public void success(SaikuCubeMetadata saikuCubeMetadata, Response response) {
            Log.d("saiku", response.toString());
            ThinQuery tq = new ThinQuery(UUID.randomUUID().toString(), cube, mdx);
            String jsonThinQuery = new Gson().toJson(tq);
            api.createThinQuery(tq.getName(), jsonThinQuery, createThinQueryCbk);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    };

    final Callback<ThinQuery> createThinQueryCbk = new Callback<ThinQuery>() {
        @Override
        public void success(ThinQuery thinQuery, Response response) {
//            String jsonThingQuery = new Gson().toJson(thinQuery);
            api.executeThinQuery(thinQuery, executeThinQueryCbk);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    };

    final Callback<QueryResult> executeThinQueryCbk = new Callback<QueryResult>() {
        @Override
        public void success(QueryResult queryResult, Response response) {

        }

        @Override
        public void failure(RetrofitError error) {
            Log.d("saiku", error.toString());
        }
    };

}
