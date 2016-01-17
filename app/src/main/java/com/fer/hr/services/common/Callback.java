package com.fer.hr.services.common;

public interface Callback<T> {
    void success(T result);
    void failure(Exception e);
}