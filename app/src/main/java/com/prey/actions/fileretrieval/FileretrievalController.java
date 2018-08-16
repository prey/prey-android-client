/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval;

import android.content.Context;
import android.os.Environment;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.managers.PreyWifiManager;
import com.prey.net.PreyWebServices;

import java.io.File;
import java.util.List;

public class FileretrievalController {

    private static FileretrievalController instance= null;
    private static Object mutex= new Object();
    private FileretrievalController(){
    }

    public static FileretrievalController getInstance(){
        if(instance==null){
            synchronized (mutex){
                if(instance==null) instance= new FileretrievalController();
            }
        }
        return instance;
    }

    public void run(Context ctx){
        PreyLogger.d("______________ FileretrievalController run _____________________");

        boolean connect = false;

        int j=0;
        do {
            connect = (PreyConfig.getPreyConfig(ctx).isConnectionExists()|| PreyWifiManager.getInstance(ctx).isOnline());

            PreyLogger.d("______________ FileretrievalController connect2+"+connect+" _____________________");
            if (connect) {
                break;
            } else {
                try{Thread.sleep(4000);}catch(Exception e){}
            }
            j++;
        }while(j<5);
        if (connect) {
            FileretrievalDatasource datasource = new FileretrievalDatasource(ctx);

            List<FileretrievalDto> list = datasource.getAllFileretrieval();
            for (int i = 0; list != null && i < list.size(); i++) {
                FileretrievalDto dto = list.get(i);
                String fileId = dto.getFileId();
                PreyLogger.d("id:" + dto.getFileId() + " " + dto.getPath());
                try {
                    FileretrievalDto dtoStatus = PreyWebServices.getInstance().uploadStatus(ctx, fileId);
                    PreyLogger.d("dtoStatus:" + dtoStatus.getStatus());
                    if (dtoStatus.getStatus() == 1) {
                        datasource.deleteFileretrieval(fileId);
                    }
                    if (dtoStatus.getStatus() == 2 || dtoStatus.getStatus() == 0) {
                        long total = dtoStatus.getTotal();
                        File file = new File(Environment.getExternalStorageDirectory() + "/" + dto.getPath());
                        PreyLogger.d("total:"+total+" size:"+dtoStatus.getSize()+" length:"+file.length());

                        int responseCode = PreyWebServices.getInstance().uploadFile(ctx, file, fileId, total);
                        PreyLogger.d("responseCode:" + responseCode);
                        if (responseCode == 200 || responseCode == 201) {
                            datasource.deleteFileretrieval(fileId);
                        }
                    }
                    if (dtoStatus.getStatus()==404){
                        datasource.deleteFileretrieval(fileId);
                    }
                } catch (Exception e) {
                    PreyLogger.e("FileretrievalController Error:" + e.getMessage(),e);

                }

            }
        }
    }

    public void deleteAll(Context ctx){
        FileretrievalDatasource datasource = new FileretrievalDatasource(ctx);
        datasource.deleteAllFileretrieval();
    }
}
