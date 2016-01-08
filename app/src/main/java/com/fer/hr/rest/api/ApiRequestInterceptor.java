package com.fer.hr.rest.api;

import com.fer.hr.App;
import com.fer.hr.data.Profile;

import retrofit.RequestInterceptor;

/**
 * Created by igor on 04/01/16.
 */
public class ApiRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        Profile appProfile = new Profile(App.getAppContext());

        request.addHeader("Authorization", appProfile.getAuthenticationToken());
    }
}
