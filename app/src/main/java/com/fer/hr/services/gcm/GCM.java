package com.fer.hr.services.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.fer.hr.data.Profile;
import com.fer.hr.services.common.Callback;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by igor on 03/01/16.
 */
public class GCM implements IGCM {
    public static String TAG = GCM.class.getSimpleName();

    private String projectId;
    private Context ctx;
    private Profile appProfile;

    public GCM(Context ctx, String projectId) {
        this.ctx = ctx;
        this.projectId = projectId;
        appProfile = new Profile(ctx);
    }

    @Override
    public void registerToGCMServer(final Callback<String> callback) {
        String gcmId = getGcmId();
        if (TextUtils.isEmpty(gcmId)) {
            new GCMRegister().execute(callback);
        } else {
            callback.success(gcmId);
        }
    }

    @Override
    public String registerToGCMServer() throws IOException {
        String gcmId = getGcmId();
        if (TextUtils.isEmpty(gcmId)) {
            return register();

        } else return gcmId;
    }

    public String getGcmId() {
        String registrationId = appProfile.getGcmId();
        if (TextUtils.isEmpty(registrationId)) {
            return null;
        }

        int registeredVersion = appProfile.getAppVersion();
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return null;
        }
        return registrationId;
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.toString());
            return -1;
        }
    }

    private class GCMRegister extends AsyncTask<Callback<String>, Void, String> {
        private IOException ex;
        private Callback<String> clientCallback;

        @Override
        protected String doInBackground(Callback<String>... params) {
            clientCallback = params[0];
            String gcmToken = null;
            try {
                gcmToken = register();
            } catch (IOException ex) {
                this.ex = ex;
            }
            return gcmToken;
        }

        @Override
        protected void onPostExecute(String gcmToken) {
            if (clientCallback != null) {
                if (gcmToken != null) clientCallback.success(gcmToken);
                else clientCallback.failure(ex);
            }
        }
    }

    @Nullable
    private String register() throws IOException {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
        String gcmRegId = gcm.register(projectId);

        appProfile.setGcmId(gcmRegId);
        appProfile.setAppVersion(getAppVersion());
        return gcmRegId;
    }
}