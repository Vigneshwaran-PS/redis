package com.redisconcepts.utils;

import com.google.gson.Gson;

public class JsonUtils {

    private static final Gson gson;

    static {
        gson = new Gson();
    }


    public static String toJson(Object o){
        return gson.toJson(o);
    }

    public static Object fromJaon(String s, Class convertTo){
        return gson.fromJson(s,convertTo);
    }


}
