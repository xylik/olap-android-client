package com.fer.hr.services;

import com.fer.hr.App;
import com.fer.hr.data.Constants;
import com.fer.hr.services.authentication.IAuthenticate;
import com.fer.hr.services.authentication.SaikuRestAuthentication;
import com.fer.hr.services.common.IService;
import com.fer.hr.services.common.ServiceException;
import com.fer.hr.services.gcm.GCM;
import com.fer.hr.services.gcm.IGCM;
import com.fer.hr.services.repository.IRepository;
import com.fer.hr.services.repository.SaikuRestRepository;

/**
 * Created by igor on 17/01/16.
 */
public class ServiceProvider {
    public static final int REPOSITORY = 1;
    public static final int AUTHENTICATION = 2;
    public static final int GCM = 3;

    private static IService authenticationService;
    private static IService repositoryService;
    private static IService gcmService;

    private ServiceProvider() {}

    public static synchronized  IService getService(int serviceType) {
        switch (serviceType) {
            case AUTHENTICATION: {
                if(authenticationService == null) authenticationService = SaikuRestAuthentication.instance();
                return authenticationService;
            }
            case REPOSITORY:{
                if(repositoryService == null) repositoryService = SaikuRestRepository.instance();
                return repositoryService;
            }
            case GCM: {
                if(gcmService==null) gcmService = com.fer.hr.services.gcm.GCM.instance();
                return gcmService;
            }
            default:
                throw new ServiceException("Unknown service!");
        }
    }
}
