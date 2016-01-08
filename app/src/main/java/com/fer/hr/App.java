package com.fer.hr;

import android.app.Application;
import android.content.Context;

import com.fer.hr.gcm.GCMService;
import com.fer.hr.rest.api.ApiRequestInterceptor;
import com.fer.hr.rest.api.Constants;
import com.fer.hr.rest.api.SaikuApi;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Properties;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by igor on 04/01/16.
 */
public class App extends Application {
    private static final String PROJECT_SETTINGS_PATH = "security/project.properties";
    private static final String PROJECT_ID_KEY = "PROJECT_ID";
    private static final String API_URL_KEY = "API_URL";
    private static Context ctx;
    public static SaikuApi api;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;

        try {
            InputStream is = getAssets().open(PROJECT_SETTINGS_PATH);
            Properties p = new Properties();
            p.load(is);
            Constants.API_URL = p.getProperty(API_URL_KEY);
            Constants.PROJECT_ID = p.getProperty(PROJECT_ID_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GCMService.instance(this, Constants.PROJECT_ID).registerWithGCMServer(null);

        OkHttpClient client = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API_URL)
                .setClient(new OkClient(client))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new ApiRequestInterceptor())
                .build();
        api = adapter.create(SaikuApi.class);
    }

    public static Context getAppContext() {
        return ctx;
    }
}
