package com.fer.hr.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.fer.hr.model.Report;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igor on 04/01/16.
 */
public class Profile {
    private SharedPreferences prefs;

    public Profile(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
    }

    private static final String GCM_ID = "com.fer.hr.GCM_ID";
    private static final String APP_VERSION = "com.fer.hr.APP_VERSION";
    private static final String AUTHENTICATION_TOKEN = "com.fer.hr.AUTHENTICATION_TOKEN";
    private static final String PUSH_REPORTS = "com.fer.hr.PUSH_REPORTS";
    private static final String PERSONAL_REPORTS = "com.fer.hr.PERSONAL_REPORTS";


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

    public void setAuthenticationToken(String token) {
        prefs.edit().putString(AUTHENTICATION_TOKEN, token).commit();
    }

    public String getAuthenticationToken() {
        return prefs.getString(AUTHENTICATION_TOKEN, null);
    }

    public void addPushReport(Report report) {
        if(TextUtils.isEmpty(report.getReportName()) || TextUtils.isEmpty(report.getMdx()) || report.getCube() == null) throw new ProfileException("invalid input!");
        List<Report> reports = getAllPushReports();
        reports.add(report);

        Type type = new TypeToken<ArrayList<Report>>() {}.getType();
        Gson gson = new Gson();
        prefs.edit().putString(PUSH_REPORTS, gson.toJson(reports, type)).commit();
    }

    public void removePushReport(Report report) {
        if(report == null) throw new ProfileException("invalid input!");
        List<Report> reports = getAllPushReports();
        reports.remove(report);

        Type type = new TypeToken<ArrayList<Report>>() {}.getType();
        Gson gson = new Gson();
        prefs.edit().putString(PUSH_REPORTS, gson.toJson(reports, type)).commit();
    }

    public List<Report> getAllPushReports() {
        String json = prefs.getString(PUSH_REPORTS, null);
        List<Report> reports = new ArrayList<>();

        Gson gson = new Gson();
        Type type = new TypeToken<List<Report>>() {}.getType();
        if(json != null) reports = gson.fromJson(json, type);

        return reports;
    }

    public void addPersonalReport(Report report) {
        if(TextUtils.isEmpty(report.getReportName()) || TextUtils.isEmpty(report.getMdx()) || report.getCube() == null) throw new ProfileException("invalid input!");
        List<Report> reports = getAllPersonalReports();
        reports.add(report);

        Type type = new TypeToken<ArrayList<Report>>() {}.getType();
        Gson gson = new Gson();
        prefs.edit().putString(PERSONAL_REPORTS, gson.toJson(reports, type)).commit();
    }

    public void removePersonalReport(Report report) {
        if(report == null) throw new ProfileException("invalid input!");
        List<Report> reports = getAllPersonalReports();
        reports.remove(report);

        Type type = new TypeToken<ArrayList<Report>>() {}.getType();
        Gson gson = new Gson();
        prefs.edit().putString(PERSONAL_REPORTS, gson.toJson(reports, type)).commit();
    }

    public List<Report> getAllPersonalReports() {
        String json = prefs.getString(PERSONAL_REPORTS, null);
        List<Report> reports = new ArrayList<>();
        reports.add(new Report("AD HOC", null, null));

        Gson gson = new Gson();
        Type type = new TypeToken<List<Report>>() {}.getType();
        if(json != null) reports = gson.fromJson(json, type);

        return reports;
    }
}