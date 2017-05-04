/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.offline;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.managers.PreyWifiManager;
import com.prey.net.PreyHttpResponse;
import com.prey.net.UtilConnection;
import com.prey.net.http.EntityFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OfflineController {

    private static OfflineController instance= null;
    private static Object mutex= new Object();
    private OfflineController(){
    }

    public static OfflineController getInstance(){
        if(instance==null){
            synchronized (mutex){
                if(instance==null) instance= new OfflineController();
            }
        }
        return instance;
    }

    public boolean connect(Context ctx){
        boolean connect = false;
        int j=0;
        do {
            connect = (PreyConfig.getPreyConfig(ctx).isConnectionExists()|| PreyWifiManager.getInstance(ctx).isOnline());
            PreyLogger.d("______________ OfflineController connect+"+connect+" _____________________");
            if (connect) {
                break;
            } else {
                try{Thread.sleep(2000);}catch(Exception e){}
            }
            j++;
        }while(j<10);
        return connect;
    }

    public void run(Context ctx) {
        PreyLogger.d("______________ OfflineController run _____________________");
        if (connect(ctx)) {
            OfflineDatasource datasource = new OfflineDatasource(ctx);
            PreyConfig preyConfig=PreyConfig.getPreyConfig(ctx);
            List<OfflineDto> list = datasource.getAllOffline();
            PreyLogger.d("list size:"+(list==null?-1:list.size()));
            for (int i = 0; list != null && i < list.size(); i++) {
                OfflineDto dto = list.get(i);
                String url=dto.getUrl();
                String requestMethod=dto.getRequestMethod();
                String contentType=dto.getContentType();
                String authorization=dto.getAuthorization();
                String status=dto.getStatus();
                String files=dto.getFiles();
                String parameters=dto.getParameters();
                List<EntityFile> entityFiles=null;
                String correlationId=dto.getCorrelationId();

                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" url:"+url);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" requestMethod:" + requestMethod);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" contentType:" + contentType);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" authorization:" + authorization);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" status:" + status);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" correlationId:" + correlationId);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" parameters:" + parameters);
                PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" files:" + files);
                Map<String, String> params=new HashMap<String, String>();
                try{
                    if(dto.getParameters()!=null) {
                        PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" Parameters:" + dto.getParameters());
                        Properties props = new Properties();
                        props.load(new StringReader(dto.getParameters().substring(1, dto.getParameters().length() - 1).replace(", ", "\n")));
                        for (Map.Entry<Object, Object> e : props.entrySet()) {
                            String key = (String) e.getKey();
                            String val = (String) e.getValue();
                            PreyLogger.d("[" + i + "]id:"+dto.getOfflineId()+" params key:" + key + " val:" + val);
                            params.put(key, val);
                        }
                    }
                }catch(Exception e){
                }
                List<File> listFiles=new ArrayList<File>();
                try{
                    if(dto.getFiles()!=null&&!"".equals(dto.getFiles())) {
                        entityFiles=new ArrayList<EntityFile>();
                        String json="{\"files\":"+files+"}";
                        PreyLogger.d("[" + i + "]json:"+json);
                        JSONObject jsnobject = new JSONObject(json);
                        JSONArray jsonArray = jsnobject.getJSONArray("files");
                        for (int j = 0; j < jsonArray.length(); j++) {
                            JSONObject jsonFile= (JSONObject)jsonArray.get(j) ;
                            String idFile=jsonFile.getString("idFile");
                            PreyLogger.d("idFile:"+idFile );
                            EntityFile entityFile=new EntityFile();
                            entityFile.setType(jsonFile.getString("type"));
                            entityFile.setName(jsonFile.getString("name"));
                            entityFile.setMimeType(jsonFile.getString("mimeType"));
                            entityFile.setLength(Integer.parseInt(jsonFile.getString("length")));
                            File file = new File("/sdcard/prey/file" + idFile+".jpg");
                            if(file.exists()) {
                                InputStream fileInputStream = new FileInputStream(file);
                                entityFile.setFile(fileInputStream);
                                entityFiles.add(entityFile);
                                listFiles.add(file);
                            }
                        }

                    }
                }catch(Exception e){
                }
                try{
                    PreyHttpResponse response=UtilConnection.connection(preyConfig, url,  params, requestMethod, contentType, authorization, status,entityFiles, correlationId) ;
                    if(response!=null){
                        PreyLogger.i("["+i+"]id:"+dto.getOfflineId()+" StatusCode:"+response.getStatusCode());
                        if (response.getStatusCode()==200||response.getStatusCode()==201){
                            datasource.deleteOffline(dto.getOfflineId());
                            for (File file:listFiles) {
                                if(file.exists()) {
                                    PreyLogger.i(" delete :"+file.getPath()+" "+file.getName());
                                    file.delete();
                                }
                            }
                        }
                    }
                }catch(Exception e){
                }
            }
        }
    }
}
