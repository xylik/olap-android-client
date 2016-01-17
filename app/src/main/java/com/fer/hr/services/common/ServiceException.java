package com.fer.hr.services.common;

/**
 * Created by igor on 17/01/16.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String detailMessage) {
        super(detailMessage);
    }

    public ServiceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ServiceException(Throwable throwable) {
        super(throwable);
    }
}
