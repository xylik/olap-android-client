package com.fer.hr.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fer.hr.App;
import com.fer.hr.R;
import com.fer.hr.data.Profile;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by igor on 03/01/16.
 */
public class GCMService {
    public static String TAG = GCMService.class.getSimpleName();
    //TODO git: move to text application properties file and add to gitignore
    private static final String PROJECT_ID_KEY = "PROJECT_ID";
    private static String PROJECT_ID;

    private static GCMService instance;
    private Context ctx;
    private Profile appProfile;

    private GCMService(Context ctx) {
        this.ctx = ctx;
        appProfile = new Profile(ctx);

        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = App.getAppContext().getResources().openRawResource(R.raw.project);
            properties.load(is);
        }catch(Resources.NotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        PROJECT_ID = properties.getProperty(PROJECT_ID_KEY);
    }

    public static synchronized GCMService instance(Context ctx) {
        if(instance == null) instance = new GCMService(ctx);
        return instance;
    }

    public String getGcmId() {
        return getRegistrationId();
    }

    public void registerWithGCMServer(Callback resultListener) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);

        String gcmId = getRegistrationId();
        if (TextUtils.isEmpty(gcmId)) {
            registerInBackground(resultListener);
        }else {
            if(resultListener != null) resultListener.success(gcmId);
        }
    }

    private void registerInBackground(final Callback resultListener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
                String gcmRegId = null;
                try {
                     gcmRegId = gcm.register(PROJECT_ID);
                } catch (IOException ex) {
                    Log.d(TAG, ex.toString());
                    if(resultListener != null) resultListener.failure(ex.toString());
                }
                if(resultListener != null) resultListener.success(gcmRegId);
                return gcmRegId;
            }

            @Override
            protected void onPostExecute(String gcmRegId) {
                appProfile.setGcmId(gcmRegId);
                appProfile.setAppVersion(getAppVersion());
            }
        }.execute();
    }

    private String getRegistrationId() {
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

    public static interface Callback {
        void success(String gcmRegToken);

        void failure(String errorMsg);
    }
}