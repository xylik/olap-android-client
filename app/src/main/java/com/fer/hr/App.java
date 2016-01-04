package com.fer.hr;

import android.app.Application;
import android.content.Context;

import com.fer.hr.gcm.GCMService;
import com.fer.hr.olap.rest.api.Constants;
import com.fer.hr.olap.rest.api.SaikuApi;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by igor on 04/01/16.
 */
public class App extends Application {
    private static Context ctx;
    public static SaikuApi api;

    @Override
    public void onCreate() {
        super.onCreate();

        ctx = getAppContext();
        GCMService.instance(this).registerWithGCMServer(null);

        OkHttpClient client = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API_URL)
                .setClient(new OkClient(client))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                //.setRequestInterceptor()
                .build();
        api = adapter.create(SaikuApi.class);
    }

    public static Context getAppContext() {
        return ctx;
    }
}
