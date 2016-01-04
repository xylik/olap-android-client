package com.fer.hr.data;

/**
 * Created by igor on 03/01/16.
 */
public class ProfileException extends RuntimeException {

    public ProfileException(String errMsg) {
        super(errMsg);
    }

    public ProfileException(String errMsg, Throwable exception) {
        super(errMsg, exception);
    }
}
