package com.fer.hr.services.gcm;

import com.fer.hr.services.common.Callback;
import com.fer.hr.services.common.IService;

import java.io.IOException;

/**
 * Created by igor on 17/01/16.
 */
public interface IGCM extends IService {
    public void registerToGCMServer(final Callback<String> callback);

    public String registerToGCMServer() throws IOException;
}