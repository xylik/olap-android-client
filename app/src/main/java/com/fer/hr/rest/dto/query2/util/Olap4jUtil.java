package com.fer.hr.rest.dto.query2.util;

/**
 * Created by igor on 18/12/15.
 */
public class Olap4jUtil {

    /**
     * Returns true if two objects are equal, or are both null.
     *
     * @param t1 First value
     * @param t2 Second value
     * @return Whether values are both equal or both null
     */
    public static <T> boolean equal(T t1, T t2) {
        return t1 == null ? t2 == null : t1.equals(t2);
    }
}
