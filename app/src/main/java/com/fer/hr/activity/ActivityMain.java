package com.fer.hr.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.fer.hr.App;
import com.fer.hr.R;
import com.fer.hr.data.Profile;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
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
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class ActivityMain extends AppCompatActivity {
    private static final String CUBE_METADATA_PATH = "queries/testCube.properties";
    public static SaikuApi api;
    public static SaikuCube cube;
    public static ThinQuery query;
    private Profile appProfile;
    private QueryResult qr;

    public static String mdx = "SELECT NON EMPTY {[Location].[Place name].Members} ON COLUMNS, NON EMPTY {[Product].[Product level].Members} ON ROWS FROM [Sales cube]";

    private CompositeSubscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appProfile = new Profile(this);


//        api.createSession("admin", "admin", "en", new ResponseImp());
//        api.getConnectionsMetadata("admin", System.currentTimeMillis(), connectionsMetaCbk);

        String credentials = "Basic aWdvckBnbWFpbC5jb206bG96aW5rYQ==";

        subscription = new CompositeSubscription();

        Observable<String> obs = App.api.login(credentials);
        subscription.add(
                obs.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("COMPLETED");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("e = " + e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("s = " + s);
                        appProfile.setAuthenticationToken(s);
                        executeThinQuery();
                    }
                }));

    }

    public void executeThinQuery() {
        String mdxQuery = "SELECT NON EMPTY {[Location].[Place name].Members} ON COLUMNS, NON EMPTY {[Product].[Product level].Members} ON ROWS FROM [Sales cube]";
        SaikuCube cube = loadCubeDefinitionFromAssets(CUBE_METADATA_PATH);
        ThinQuery tq = new ThinQuery(UUID.randomUUID().toString(), cube, mdxQuery);

        App.api.executeThinQuery(tq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryResult -> {
                    System.out.println("igor" + queryResult.getCellset().get(0).toString());
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        subscription.unsubscribe();
    }

    private SaikuCube loadCubeDefinitionFromAssets(String filePath) {
        InputStream is = null;
        try {
            is = getAssets().open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Properties p = new Properties();
        try {
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean isVisible = p.getProperty("VISIBLE").equalsIgnoreCase("true") ? true : false;
        return new SaikuCube(
                p.getProperty("CONNECTION"),
                p.getProperty("UNIQUE_NAME"),
                p.getProperty("NAME"),
                p.getProperty("CAPTION"),
                p.getProperty("CATALOG"),
                p.getProperty("SCHEMA"),
                isVisible);
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
//            api.getCubeMetadata("admin", connection, catalog, schema, cube, key, System.currentTimeMillis(), cubeMetaCbk);
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
