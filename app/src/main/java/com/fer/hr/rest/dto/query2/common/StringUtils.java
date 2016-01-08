package com.fer.hr.rest.dto.query2.common;

/**
 * Created by igor on 18/12/15.
 */
public class StringUtils {

    public static boolean isNotBlank(String text) {
        return !(text == null  || text.isEmpty());
    }
}
