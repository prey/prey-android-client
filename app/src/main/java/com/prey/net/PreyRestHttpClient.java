/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;


import java.io.File;

import java.net.HttpURLConnection;

import java.util.List;
import java.util.Map;


import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import com.prey.net.http.EntityFile;



public class PreyRestHttpClient {

    private static PreyRestHttpClient _instance = null;
    private Context ctx = null;

    private PreyRestHttpClient(Context ctx) {
        this.ctx = ctx;

    }

    public static PreyRestHttpClient getInstance(Context ctx) {

        _instance = new PreyRestHttpClient(ctx);
        return _instance;

    }

    private static final String CONTENT_TYPE_URL_ENCODED="application/x-www-form-urlencoded";

    public PreyHttpResponse post(String url, Map<String, String> params) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
        PreyHttpResponse response=UtilConnection.connectionPost(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse postAutentication(String url, Map<String, String> params) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
        PreyHttpResponse response=UtilConnection.connectionPostAuthorization(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse postAutentication(String url, Map<String, String> params, List<EntityFile> entityFiles) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
        String contentType=CONTENT_TYPE_URL_ENCODED;
        if(entityFiles!=null&&entityFiles.size()>0)
            contentType="";
        PreyHttpResponse response=UtilConnection.connectionPostAuthorization(PreyConfig.getPreyConfig(ctx),url,params,contentType,entityFiles);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }



    public PreyHttpResponse postAutenticationTimeout(String url, Map<String, String> params) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
        PreyHttpResponse response=UtilConnection.connectionPostAuthorization(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }


    public PreyHttpResponse postStatusAutentication(String url, String status, Map<String, String> params) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
        PreyHttpResponse response=UtilConnection.connectionPostAuthorizationStatus(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED,status);
        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse postAutenticationCorrelationId(String url, String status, String correlation,Map<String, String> params) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString());
        PreyHttpResponse response=UtilConnection.connectionPostAuthorizationCorrelationId(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED,status,correlation);
        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse get(String url, Map<String, String> params) throws Exception {
        PreyHttpResponse response=UtilConnection.connectionGet(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse getAutentication(String url, Map<String, String> params) throws Exception {
        PreyHttpResponse response=UtilConnection.connectionGetAuthorization(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse get(String url, Map<String, String> params, String user, String pass) throws Exception {
        PreyHttpResponse response=UtilConnection.connectionGetAuthorization(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED,user,pass);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse get(String url, Map<String, String> params, String user, String pass,String content) throws Exception {
        PreyHttpResponse response=UtilConnection.connectionGetAuthorization(PreyConfig.getPreyConfig(ctx),url,params,content,user,pass);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }

    public PreyHttpResponse delete(String url, Map<String, String> params) throws Exception {
        PreyHttpResponse response=UtilConnection.connectionDeleteAuthorization(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }


    public int postJson(String url, JSONObject jsonParam) {
        int httpResult = -1;
        HttpURLConnection connection=null;
        try {
            PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + jsonParam.toString());
            connection=UtilConnection.connectionPostJson(PreyConfig.getPreyConfig(ctx),url,jsonParam,null);
            PreyHttpResponse response = new PreyHttpResponse(connection);
            PreyLogger.d("Response from server: " + response.toString());
            httpResult=connection.getResponseCode();
        } catch (Exception e) {
            PreyLogger.e("postJson error:" + e.getMessage(), e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return httpResult;
    }

    public int postJsonAutentication(String url, JSONObject jsonParam) {
        int httpResult = -1;
        HttpURLConnection connection=null;
        try {
            PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + jsonParam.toString());
            connection=UtilConnection.connectionPostJsonAuthorization(PreyConfig.getPreyConfig(ctx),url,jsonParam);
            PreyHttpResponse response = new PreyHttpResponse(connection);
            PreyLogger.d("Response from server: " + response.toString());
            httpResult=connection.getResponseCode();
        } catch (Exception e) {
            PreyLogger.e("postJsonAutentication error:" + e.getMessage(), e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return httpResult;
    }

    public int uploadFile(Context ctx,String url, File file,long total) {
        return UtilConnection.uploadFile(PreyConfig.getPreyConfig(ctx), url, file,total);
    }


    public PreyHttpResponse connectionPostAuthorizationCorrelationId(String url, String status, Map<String, String> params,String correlationId) throws Exception {
        PreyLogger.d("Sending using 'POST' - URI: " + url + " - parameters: " + params.toString()+" status:"+status+" correlationId:"+correlationId);
        PreyHttpResponse response=UtilConnection.connectionPostAuthorizationCorrelationId(PreyConfig.getPreyConfig(ctx),url,params,CONTENT_TYPE_URL_ENCODED,status,correlationId);

        PreyLogger.d("Response from server: " + (response==null?"":response.toString()));
        return response;
    }


}