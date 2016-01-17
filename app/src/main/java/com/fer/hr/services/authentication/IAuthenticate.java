package com.fer.hr.services.authentication;

import com.fer.hr.services.common.Callback;
import com.fer.hr.services.common.IService;

/**
 * Created by igor on 17/01/16.
 */
public interface IAuthenticate extends IService {

    void login(String userName, String password, final Callback<String> callback);

    void register(String userName, String password, final Callback<String> callback);

}
