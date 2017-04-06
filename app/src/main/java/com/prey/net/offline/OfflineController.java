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
                    if(dto.getFiles()!=null) {
                        entityFiles=new ArrayList<EntityFile>();
                        Properties props = new Properties();
                        String corte=dto.getFiles().substring(1, dto.getFiles().length() - 1);
                        corte=corte.replace("}, ", "\n");
                        corte=corte.replace("{", "");
                        corte=corte.replace("}", "");
                        props.load(new StringReader(corte));
                        for (Map.Entry<Object, Object> e : props.entrySet()) {
                            String idFile = (String) e.getKey();
                            String val = (String) e.getValue();
                            PreyLogger.d("idFile:"+idFile+" val:"+val);
                            Map<String, String> filesMap=new HashMap<String, String>();
                            Properties props2 = new Properties();
                            props2.load(new StringReader(val.substring(0, val.length() - 1).replace(", ", "\n")));
                            for (Map.Entry<Object, Object> e2 : props2.entrySet()) {
                                String key2 = (String) e2.getKey();
                                String val2 = (String) e2.getValue();
                                PreyLogger.d("key2:"+key2+" val2:"+val2);
                                filesMap.put(key2, val2);
                            }
                            EntityFile entityFile=new EntityFile();
                            entityFile.setType(filesMap.get("type"));
                            entityFile.setName(filesMap.get("name"));
                            entityFile.setMimeType(filesMap.get("mimeType"));
                            entityFile.setLength(Integer.parseInt(filesMap.get("length")));
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
