package com.fer.hr.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by igor on 04/01/16.
 */
public class Profile {
    private SharedPreferences prefs;

    public Profile(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
    }

    public static final String GCM_ID = "com.fer.hr.GCM_ID";
    private static final String APP_VERSION = "com.fer.hr.APP_VERSION";

    public void setGcmId(String gcmId) {
        if (TextUtils.isEmpty(gcmId)) throw new ProfileException("gcmId can't be empty");
        prefs.edit().putString(GCM_ID, gcmId).commit();
    }

    public String getGcmId() {
        return prefs.getString(GCM_ID, null);
    }

    public void setAppVersion(int version) {
        prefs.edit().putInt(APP_VERSION, version).commit();
    }

    public int getAppVersion() {
        return prefs.getInt(APP_VERSION, -1);
    }

}
