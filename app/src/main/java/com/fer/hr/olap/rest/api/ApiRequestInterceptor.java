package com.fer.hr.olap.rest.api;

import retrofit.RequestInterceptor;

/**
 * Created by igor on 04/01/16.
 */
public class ApiRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        //request.addHeader("Authorization", authorizationValue);
    }
}
