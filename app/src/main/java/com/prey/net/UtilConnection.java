/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.net.http.EntityFile;
import com.prey.net.http.SimpleMultipartEntity;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class UtilConnection {

    private static int RETRIES = 4;

    private static int RETRY_DELAY_MS =1000;

    private static final String REQUEST_METHOD_PUT="PUT";
    private static final String REQUEST_METHOD_POST="POST";
    private static final String REQUEST_METHOD_GET="GET";
    private static final String REQUEST_METHOD_DELETE="DELETE";
    private static final boolean USE_CACHES=false;
    private static final int CONNECT_TIMEOUT=30000;
    private static final int READ_TIMEOUT=30000;

    private static String getCredentials(String user, String password) {
        return (Base64.encodeBytes((user + ":" + password).getBytes()));
    }

    private static String getUserAgent(PreyConfig preyConfig) {
        return "Prey/".concat(preyConfig.getPreyVersion()).concat(" (Android " + PreyUtils.getBuildVersionRelease() + ")");
    }

    private static String getAuthorization(PreyConfig preyConfig) {
        PreyLogger.d("getAuthorization:("+preyConfig.getApiKey()+",X)");
        return "Basic " + getCredentials(preyConfig.getApiKey(), "X");
    }

    private static String getAuthorization(String user,String pass) {
        PreyLogger.d("getAuthorization:("+user+","+pass+")");
        return "Basic " + getCredentials(user, pass);
    }

    public static final PreyHttpResponse connectionPut(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_PUT,contentType,null,null,null,null);
    }
    public static final PreyHttpResponse connectionGet(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_GET,contentType,null,null,null,null);
    }
    public static final PreyHttpResponse connectionGetAuthorization(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_GET,contentType,getAuthorization(preyConfig),null,null,null);
    }
    public static final PreyHttpResponse connectionGetAuthorization(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType,String user,String pass) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_GET,null, getAuthorization(user, pass),null,null,null);
    }
    public static final PreyHttpResponse connectionDelete(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_DELETE,contentType,null,null,null,null);
    }
    public static final PreyHttpResponse connectionDeleteAuthorization(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_DELETE,contentType,getAuthorization(preyConfig),null,null,null);
    }
    public static final PreyHttpResponse connectionPost(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_POST,contentType,null,null,null,null);
    }
    public static final PreyHttpResponse connectionPostAuthorization(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_POST,contentType,getAuthorization(preyConfig),null,null,null);
    }
    public static final PreyHttpResponse connectionPostAuthorization(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType,List<EntityFile> entityFiles) throws Exception {
        return connection(preyConfig, uri, params, REQUEST_METHOD_POST, contentType,getAuthorization(preyConfig), null,entityFiles,null);
    }
    public static final PreyHttpResponse connectionPostAuthorizationStatus(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType,String status) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_POST,contentType,getAuthorization(preyConfig),status,null,null);
    }
    public static final PreyHttpResponse connectionPostAuthorizationCorrelationId(PreyConfig preyConfig,String uri, Map<String, String> params, String contentType,String status,String correlationId) throws Exception {
        return connection(preyConfig,uri,params,REQUEST_METHOD_POST,contentType,getAuthorization(preyConfig),status,null,correlationId);
    }

    private static final PreyHttpResponse connection(PreyConfig preyConfig,String uri, Map<String, String> params,String requestMethod,String contentType,String authorization,String status,List<EntityFile> entityFiles,String correlationId) throws Exception {
        PreyHttpResponse response=null;
        URL url = new URL(uri);
        HttpURLConnection connection=null;
        int retry = 0;
        boolean delay=false;
        PreyLogger.d("uri:"+uri);
        SimpleMultipartEntity multiple=new SimpleMultipartEntity();
        do {
            if (delay) {
                Thread.sleep(RETRY_DELAY_MS);
            }
            if (uri.indexOf("https:")>=0) {
                connection = (HttpsURLConnection) url.openConnection();
            }else{
                connection = (HttpURLConnection) url.openConnection();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Accept", "*/*");
            if(contentType!=null) {
                connection.addRequestProperty("Content-Type", contentType);
            }
            if (authorization!=null) {
                connection.addRequestProperty("Authorization", authorization);
            }
            if (status!=null) {
                connection.addRequestProperty("X-Prey-Status", status);
                PreyLogger.i("X-Prey-Status:"+status);
            }

            if (correlationId!=null) {
                connection.addRequestProperty("X-Prey-Correlation-ID", correlationId);
                PreyLogger.i("X-Prey-Correlation-ID:"+correlationId);
                String deviceId=preyConfig.getDeviceId();
                connection.addRequestProperty("X-Prey-Device-ID", deviceId);
                PreyLogger.i("X-Prey-Device-ID:"+deviceId);
                connection.addRequestProperty("X-Prey-State", status);
                PreyLogger.i("X-Prey-State:"+status);

            }


            connection.addRequestProperty("User-Agent", getUserAgent(preyConfig));
            if (entityFiles==null&&(params!=null&&params.size()>0)){
                OutputStream os = connection.getOutputStream();
                DataOutputStream dos = new DataOutputStream( os );
                dos.writeBytes(getPostDataString(params));
            }
            if( entityFiles!=null&&entityFiles.size()>0 ) {
                for(Map.Entry<String, String> entry : params.entrySet()){
                    String key=  entry.getKey();
                    String value=null;
                    try{
                        value=  entry.getValue() ;
                    }catch(Exception e){
                    }
                    if(value==null){
                        value="";
                    }
                    multiple.addPart(key,value);
                }
                for(int i=0;entityFiles!=null&&i<entityFiles.size();i++) {
                    EntityFile entityFile = entityFiles.get(i);
                    boolean isLast = ((i + 1) == entityFiles.size() ? true : false);
                    multiple.addPart(entityFile.getType(), entityFile.getName(), entityFile.getFile(), entityFile.getMimeType(), isLast);
                }
                connection.setRequestProperty("Content-Length", "" + multiple.getContentLength());
                connection.setRequestProperty("Content-Type", multiple.getContentType());
                OutputStream os = connection.getOutputStream();
                multiple.writeTo(os);
            }
            int responseCode=connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            PreyLogger.i("responseCode:"+responseCode+" responseMessage:"+responseMessage+" uri:"+uri);
            switch (responseCode) {
                case HttpURLConnection.HTTP_CREATED:
                    PreyLogger.i(uri + " **CREATED**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_OK:
                    PreyLogger.i(uri + " **OK**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_CONFLICT:
                    PreyLogger.i(uri + " **CONFLICT**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_FORBIDDEN:
                    PreyLogger.i(uri + " **FORBIDDEN**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    PreyLogger.i(uri + " **MOVED_TEMP**");
                    return convertPreyHttpResponse(responseCode,connection);
                case 422:
                    PreyLogger.i(uri + " **422**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_BAD_GATEWAY:
                    PreyLogger.i(uri + " **BAD_GATEWAY**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    PreyLogger.i(uri + " **INTERNAL_ERROR**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    PreyLogger.i(uri + " **NOT_FOUND**");
                    return convertPreyHttpResponse(responseCode,connection);
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    PreyLogger.i(uri + " **gateway timeout**");
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    PreyLogger.i(uri + "**unavailable**");
                    break;
                default:
                    PreyLogger.i(uri + " **unknown response code**.");
                    break;
            }
            connection.disconnect();
            retry++;
            PreyLogger.i("Failed retry " + retry + "/" + RETRIES);
            delay = true;
        } while (retry < RETRIES);
        return response;
    }

    private static PreyHttpResponse convertPreyHttpResponse(int responseCode,HttpURLConnection connection)throws Exception {
        StringBuffer sb = new StringBuffer();
        if(responseCode==200||responseCode==201) {
            InputStream input = connection.getInputStream();
            if (input != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                String decodedString;

                while ((decodedString = in.readLine()) != null) {
                    sb.append(decodedString);
                    sb.append('\r');
                }
                in.close();
            }
            PreyLogger.i(sb.toString());
        }
        Map<String, List<String>> mapHeaderFields=connection.getHeaderFields();

        connection.disconnect();
        return new PreyHttpResponse(responseCode,sb.toString(),mapHeaderFields);
    }
    
    public static HttpURLConnection connectionPostJson(PreyConfig preyConfig,String uri, JSONObject jsonParam) {
        return connectionPostJson(preyConfig,uri,jsonParam,null);
    }
    
    public static HttpURLConnection connectionPostJsonAuthorization(PreyConfig preyConfig,String uri, JSONObject jsonParam) {
        return connectionPostJson(preyConfig,uri,jsonParam,"Basic " + getCredentials(preyConfig.getApiKey(), "X"));
    }

    public static HttpURLConnection connectionPostJson(PreyConfig preyConfig,String uri, JSONObject jsonParam, String authorization) {
        HttpURLConnection connection=null;
        int httpResult = -1;
        try {
            URL url = new URL(uri);
            PreyLogger.i("postJson page:" + uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(REQUEST_METHOD_POST);
            connection.setUseCaches(USE_CACHES);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Content-Type", "application/json");
            if (authorization!=null)
                connection.addRequestProperty("Authorization", authorization);
            connection.addRequestProperty("User-Agent",getUserAgent(preyConfig));
            connection.addRequestProperty("Origin", "android:com.prey");
            connection.connect();
            PreyLogger.d("jsonParam.toString():" + jsonParam.toString());
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(jsonParam.toString());
            out.close();
            httpResult = connection.getResponseCode();
            PreyLogger.i("postJson responseCode:"+httpResult);
        } catch (Exception e) {
            PreyLogger.e("postJson error:" + e.getMessage(), e);
        }
        return connection;
    }

    private static String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){

            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            try {
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }catch (Exception e){
                result.append(URLEncoder.encode("", "UTF-8"));
            }
        }
        return result.toString();
    }

    public static int uploadFile(PreyConfig preyConfig,String page, File file,long total){
        int responseCode =0;
        HttpURLConnection connection = null;
        OutputStream output = null;
        InputStream input =null;
        FileInputStream fileInput=null;
        PreyLogger.i("page:"+page+" upload:"+file.getName()+" length:"+file.length()+" total:"+total);
        try {
            URL url=new URL(page);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            connection.addRequestProperty("Origin", "android:com.prey");
            connection.addRequestProperty("Content-Type", "application/octet-stream");
            connection.addRequestProperty("User-Agent", getUserAgent(preyConfig));

            if(total>0){
                connection.addRequestProperty("X-Prey-Upload-Resumable",""+total);
                connection.setRequestProperty("Content-Length", "" + (file.length()-total));
                PreyLogger.i("Content-Length:"+(file.length()-total));
            }else{
                connection.setRequestProperty("Content-Length", "" + file.length());
            }
            output = connection.getOutputStream();
            fileInput=new FileInputStream(file);
            input = new BufferedInputStream(fileInput);
            int maxByte=4096;
            byte[] buffer = new byte[maxByte];
            int length;



            long dif=total-maxByte;

            long read=0;
            if (total>0) {
                int maxByte2=4096;
                byte[] buffer2 = new byte[maxByte2];

                do {
                    length=0;

                    if (total<maxByte2) {
                        buffer2 = new byte[(int)total];

                    }
                    length = input.read(buffer2);
                    read=read+length;
                    total=total-length;

                    if (total<=0)
                        break;
                    PreyLogger.i("uploadFile total:"+total+" length:"+length+" read:"+read);
                }while(total  > 0);

            }
            PreyLogger.i("uploadFile read:"+read);


            while ((length = input.read(buffer)) > 0) {

                    output.write(buffer, 0, length);




            }
            output.flush();
            String responseMessage = connection.getResponseMessage();
            responseCode = connection.getResponseCode();
            PreyLogger.i("uploadFile responseCode:"+responseCode+" responseMessage:"+responseMessage);

        } catch (Exception e) {
            PreyLogger.e("error upload:"+e.getMessage(),e);
            responseCode=0;
        } finally {
            try {
                if(input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
            try {
                if(fileInput != null) {
                    fileInput.close();
                }
            } catch (IOException e) {
            }
            try {
                if(output != null) {
                    output.close();
                }
            } catch (IOException e) {
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
        return responseCode;
    }
}
