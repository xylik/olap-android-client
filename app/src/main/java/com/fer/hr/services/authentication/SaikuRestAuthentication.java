package com.fer.hr.services.authentication;

import android.os.AsyncTask;

import com.fer.hr.App;
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

    @Override
    public boolean isLogedIn() {
        return App.getProfile().getAuthenticationToken() == null ? false : true;
    }

    @Override
    public void login(String userName, String password, Callback<String> callback) {
        String encodedCredentials = getEncodedCredentials(userName, password);

        App.api.login(encodedCredentials, new retrofit.Callback<String>() {
            @Override
            public void success(String saikuToken, Response response) {
                App.getProfile().setAuthenticationToken(saikuToken);
                if (callback != null) callback.success(saikuToken);
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null) callback.failure(error);
            }
        });
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
                    App.getProfile().setAuthenticationToken(saikuToken);
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

    private String getEncodedCredentials(String userName, String password) {
        String credentials = userName + ":" + password;
        return "Basic " + CryptoUtil.encodeBase64(credentials);
    }
}
