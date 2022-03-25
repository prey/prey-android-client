/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json;

import com.prey.PreyLogger;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class UtilJson {

    public static JSONObject makeJsonResponse(String command,String target,String status){
        JSONObject json=new JSONObject();
        try {
            json.put("command", command);
            json.put("target", target);
            json.put("status", status);
        } catch (JSONException e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return json;
    }

    public static JSONObject makeJsonResponse(String command,String target,String status,String reason){
        JSONObject json=makeJsonResponse(command, target, status);
        try {
            json.put("reason", reason);
        } catch (JSONException e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return json;
    }

    public static  Map<String, String> makeMapParam(String command,String target,String status){
        Map<String, String> map=new HashMap<String, String>();
        map.put("command", command);
        map.put("target", target);
        map.put("status", status);
        return map;
    }

    public static  Map<String, String> makeMapParam(String command,String target,String status,String reason){
        Map<String, String> map=makeMapParam(command, target, status);
        if(reason!=null)
            map.put("reason", reason);
        return map;
    }

    /**
     * Method get JSONObject
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    public static JSONObject getJSONObject(JSONObject jsonObject, String key) throws JSONException {
        JSONObject out = null;
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getJSONObject(key);
        }
        return out;
    }

    /**
     * Method get string
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    public static String getString(JSONObject jsonObject, String key) throws JSONException {
        String out = null;
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getString(key);
        }
        return out;
    }

    /**
     * Method get boolean
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    public static boolean getBoolean(JSONObject jsonObject, String key) throws JSONException {
        boolean out = false;
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getBoolean(key);
        }
        return out;
    }

    /**
     * Method get int
     *
     * @param jsonObject
     * @param key
     * @return value for key
     * @throws JSONException
     */
    public static int getInt(JSONObject jsonObject, String key) throws JSONException {
        int out = 0;
        if (jsonObject != null && jsonObject.has(key)) {
            out = jsonObject.getInt(key);
        }
        return out;
    }

}