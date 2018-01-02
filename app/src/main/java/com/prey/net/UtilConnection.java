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
import com.prey.net.offline.OfflineDatasource;
import com.prey.net.offline.OfflineDto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class UtilConnection {

    private static int[] ARRAY_RETRY_DELAY_MS =new int[]{1,2,3,4};

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
        //PreyLogger.d("getAuthorization:("+preyConfig.getApiKey()+",X)");
        return "Basic " + getCredentials(preyConfig.getApiKey(), "X");
    }

    private static String getAuthorization(String user,String pass) {
        //PreyLogger.d("getAuthorization:("+user+","+pass+")");
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

    public static final PreyHttpResponse connection(PreyConfig preyConfig,String uri, Map<String, String> params,String requestMethod,String contentType,String authorization,String status,List<EntityFile> entityFiles,String correlationId) throws Exception {
        PreyHttpResponse response=null;
        URL url = new URL(uri);
        HttpURLConnection connection=null;
        int retry = 0;
        boolean delay=false;
        PreyLogger.d("uri:"+uri);
        if(params!=null){
            Iterator<String> ite=params.keySet().iterator();
            while (ite.hasNext()){
                String key=ite.next();
                PreyLogger.d("["+key+"]:"+params.get(key));
            }
        }
        SimpleMultipartEntity multiple=new SimpleMultipartEntity();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmZ");
        List <ByteArrayOutputStream> listOutputStream = new ArrayList<>();
        try{
            int RETRIES = 4;
            do {
                if (delay) {
                    Thread.sleep(ARRAY_RETRY_DELAY_MS[retry]*1000);
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
                    PreyLogger.d("Authorization:"+authorization);
                }
                if (status!=null) {
                    connection.addRequestProperty("X-Prey-Status", status);
                    PreyLogger.d("X-Prey-Status:"+status);
                }

                if (correlationId!=null) {
                    connection.addRequestProperty("X-Prey-Correlation-ID", correlationId);
                    PreyLogger.d("X-Prey-Correlation-ID:"+correlationId);
                    String deviceId=preyConfig.getDeviceId();
                    connection.addRequestProperty("X-Prey-Device-ID", deviceId);
                    PreyLogger.d("X-Prey-Device-ID:"+deviceId);
                    connection.addRequestProperty("X-Prey-State", status);
                    PreyLogger.d("X-Prey-State:"+status);

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
                        ByteArrayOutputStream outputStream=multiple.addPart(entityFile.getType(), entityFile.getName(), entityFile.getFile(), entityFile.getMimeType(), isLast);
                        listOutputStream.add(outputStream);
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
                        PreyLogger.d(uri + " **CREATED**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_OK:
                        PreyLogger.d(uri + " **OK**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_CONFLICT:
                        PreyLogger.d(uri + " **CONFLICT**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        PreyLogger.d(uri + " **FORBIDDEN**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        PreyLogger.d(uri + " **MOVED_TEMP**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case 422:
                        PreyLogger.d(uri + " **422**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_BAD_GATEWAY:
                        PreyLogger.d(uri + " **BAD_GATEWAY**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        PreyLogger.d(uri + " **INTERNAL_ERROR**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        PreyLogger.d(uri + " **NOT_FOUND**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                        PreyLogger.d(uri + " **gateway timeout**");
                        break;
                    case HttpURLConnection.HTTP_UNAVAILABLE:
                        PreyLogger.d(uri + "**unavailable**");
                        break;
                    case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
                        PreyLogger.d(uri + " **NOT_ACCEPTABLE**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        PreyLogger.d(uri + " **HTTP_UNAUTHORIZED**");
                        response = convertPreyHttpResponse(responseCode,connection);
                        retry = RETRIES;
                        break;
                    default:
                        PreyLogger.d(uri + " **unknown response code**.");
                        break;
                }
                connection.disconnect();
                retry++;
                if(retry <= RETRIES) {
                    PreyLogger.i("Failed retry " + retry + "/" + RETRIES);
                }
                delay = true;
            } while (retry < RETRIES);
        }catch(Exception e){
            PreyLogger.d("error util:"+e.getMessage());
        }
        /*
        if(response==null||(response!=null&& !(response.getStatusCode()==200||response.getStatusCode()==201||response.getStatusCode()==404||response.getStatusCode()==406||response.getStatusCode()==409||response.getStatusCode()==502))) {
            OfflineDatasource datasource = new OfflineDatasource(preyConfig.getContext());
            if(response!=null) {
                PreyLogger.i("code:" + response.getStatusCode());
            }
            PreyLogger.i("uri:"+uri+" status:"+status);
            if(pageOffline(uri)) {
                OfflineDto offline = new OfflineDto();
                offline.setOfflineId(sdf.format(new Date()));
                offline.setUrl(uri);
                offline.setContentType(contentType);
                offline.setAuthorization(authorization);
                offline.setStatus(status);
                offline.setCorrelationId(correlationId);


                offline.setParameters(params.toString());
                offline.setRequestMethod(requestMethod);


                if (entityFiles != null && entityFiles.size() > 0) {
                    JSONArray array = new JSONArray();
                    for (int i = 0; entityFiles != null && i < entityFiles.size(); i++) {
                        EntityFile entityFile = entityFiles.get(i);


                        JSONObject json = new JSONObject();
                        json.put("idFile", entityFile.getIdFile());
                        json.put("type", entityFile.getType());
                        json.put("name", entityFile.getName());
                        json.put("mimeType", entityFile.getMimeType());
                        json.put("length", entityFile.getLength());


                        ByteArrayOutputStream outputStream = listOutputStream.get(i);
                        PreyLogger.i("idFile:" + entityFile.getIdFile() + " json:" + json.toString());
                        array.put(json);


                        saveFile(entityFile.getIdFile(), outputStream);

                    }
                    PreyLogger.i("files:" + array.toString());
                    offline.setFiles(array.toString());
                    offline.setOfflineId(sdf.format(new Date()) + "_report");
                }

                PreyLogger.i("id:" + offline.getOfflineId() + " url:" + offline.getUrl());
                PreyLogger.i("id:" + offline.getOfflineId() + " requestMethod:" + requestMethod);
                PreyLogger.i("id:" + offline.getOfflineId() + " contentType:" + contentType);
                PreyLogger.i("id:" + offline.getOfflineId() + " authorization:" + authorization);
                PreyLogger.i("id:" + offline.getOfflineId() + " status:" + status);
                PreyLogger.i("id:" + offline.getOfflineId() + " correlationId:" + correlationId);
                PreyLogger.i("id:" + offline.getOfflineId() + " files:" + offline.getFiles());


                datasource.createOffline(offline);
            }
        }*/
        return response;
    }

    public static boolean pageOffline(String uri){
        if (uri!=null && uri.indexOf("devices.json")<0 && uri.indexOf("data.json")<0 && uri.indexOf("profile.xml")<0 && uri.indexOf("signup.json")<0){
            return true;
        }
        return false;
    }

    private static void saveFile(String idFile,ByteArrayOutputStream outputStream){
        FileOutputStream fileOutputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;

        try {
            File preyDirectory = new File("/sdcard/prey/");
            preyDirectory.mkdirs();



            PreyLogger.i("idFile:"+idFile+" byteArrayInputStream null:"+(byteArrayInputStream==null));



            File file=new File("/sdcard/prey/file" + idFile+".jpg");
            fileOutputStream = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }





            fileOutputStream.write(outputStream.toByteArray());


            fileOutputStream.flush();




        } catch (Exception e) {
            PreyLogger.i("Error:"+e.getMessage());
            PreyLogger.e("Error:"+e.getMessage(),e);

        } finally {

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                }

            }

            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                }
            }

        }
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
            PreyLogger.d(sb.toString());
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
