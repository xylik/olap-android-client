package com.fer.hr.olap.rest.dto.session;

import com.google.gson.annotations.SerializedName;

/**
 * Created by igor on 17/12/15.
 */
public class Session {
    @SerializedName(value="authid")
    private String authId;
    @SerializedName(value="isadmin")
    private boolean isAdmin;
    private String language;
    private String[] roles;
    @SerializedName(value="sessionid")
    private String sessionId;
    private String username;

    public Session() {}

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
