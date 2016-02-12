package com.fer.hr.services.authentication;

import android.content.Context;
import android.os.AsyncTask;

import com.fer.hr.App;
import com.fer.hr.data.Profile;
import com.fer.hr.services.common.ServiceException;
import com.fer.hr.utils.CryptoUtil;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.gcm.IGCM;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by igor on 17/01/16.
 */
public class SaikuRestAuthentication implements IAuthenticate {
    private static final String NOT_LOGGED_IN = "User is not logged in!";

    private static SaikuRestAuthentication instance;
    private Profile appProfile;

    private SaikuRestAuthentication(){
        appProfile = App.getProfile();
    }

    public static SaikuRestAuthentication instance() {
        if(instance == null) instance = new SaikuRestAuthentication();

        return instance;
    }

    @Override
    public void register(String userName, String password, final Callback<String> clientCallback) {
        final String encodedCredentials = getEncodedCredentials(userName, password);
        final IGCM gcmService = (IGCM) ServiceProvider.getService(ServiceProvider.GCM);

        new AsyncTask<String, Void, String>() {
            private Exception ex;

            @Override
            protected String doInBackground(String... params) {
                String saikuToken = null;
                try {
                    String gcmToken = gcmService.registerToGCMServer();
                    saikuToken = App.api.registerAccount(encodedCredentials, gcmToken);
                    appProfile.setAuthenticationToken(saikuToken);
                }catch (Exception ex) {
                    this.ex = ex;
                    saikuToken = null;
                }
                return saikuToken;
            }

            @Override
            protected void onPostExecute(String saikuToken) {
                if(clientCallback != null){
                    if(saikuToken != null) clientCallback.success(saikuToken);
                    else clientCallback.failure(this.ex);
                }
            }
        }.execute();
    }

    @Override
    public void login(String userName, String password, Callback<String> callback) {
        String encodedCredentials = getEncodedCredentials(userName, password);

        App.api.login(encodedCredentials, new retrofit.Callback<String>() {
            @Override
            public void success(String saikuToken, Response response) {
                appProfile.setAuthenticationToken(saikuToken);
                if (callback != null) callback.success(saikuToken);
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) callback.failure(error);
            }
        });
    }

    @Override
    public boolean isLogedIn() {
        return appProfile.getAuthenticationToken() == null ? false : true;
    }

    @Override
    public void logout(Callback<String> callback) {
        if(!isLogedIn() && callback != null) callback.failure( new ServiceException(NOT_LOGGED_IN));

        App.api.logout(appProfile.getAuthenticationToken(), new retrofit.Callback<String>() {
            @Override
            public void success(String successMsg, Response response) {
                if (callback != null) callback.success(successMsg);
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) callback.failure(error);
            }
        });

        appProfile.setAuthenticationToken(null);
    }

    private String getEncodedCredentials(String userName, String password) {
        String credentials = userName + ":" + password;
        return "Basic " + CryptoUtil.encodeBase64(credentials);
    }

}