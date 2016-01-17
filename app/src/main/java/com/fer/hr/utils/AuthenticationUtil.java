package com.fer.hr.utils;


import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by igor on 05/01/16.
 */
public class AuthenticationUtil {

    private AuthenticationUtil() {}

    public static String encodeBase64(String text) {
        byte[] encodedBytes = null;
        try {
            encodedBytes = Base64.encode(text.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String encodedString = null;
        try {
            encodedString = new String(encodedBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedString;
    }
}
