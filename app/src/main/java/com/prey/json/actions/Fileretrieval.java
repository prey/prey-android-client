/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.os.Environment;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.fileretrieval.FileretrievalDatasource;
import com.prey.actions.fileretrieval.FileretrievalDto;
import com.prey.actions.fileretrieval.FileretrievalOpenHelper;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Fileretrieval {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        int responseCode=0;
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        String reason = null;
        try {
            String jobId = parameters.getString(PreyConfig.JOB_ID);
            PreyLogger.d("jobId:"+jobId);
            if(jobId!=null&&!"".equals(jobId)){
                reason="{\"device_job_id\":\""+jobId+"\"}";
            }
        } catch (Exception e) {
        }
        try {
            PreyLogger.d("Fileretrieval started");
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "fileretrieval", "started",reason));
            String path = parameters.getString("path");
            String fileId = parameters.getString("file_id");
            if(fileId==null||"".equals(fileId)||"null".equals(fileId)){
                throw new Exception("file_id null");
            }
            File file=new File(Environment.getExternalStorageDirectory()+"/"+path);
            FileretrievalDto fileDto=new FileretrievalDto();
            fileDto.setFileId(fileId);
            fileDto.setPath(path);
            fileDto.setSize(file.length());
            fileDto.setStatus(0);
            FileretrievalDatasource datasource=new FileretrievalDatasource(ctx);
            datasource.createFileretrieval(fileDto);
            PreyLogger.d("Fileretrieval started uploadFile");
            responseCode=PreyWebServices.getInstance().uploadFile(ctx, file, fileId,0);
            PreyLogger.d("Fileretrieval responseCode uploadFile :"+responseCode);
            if(responseCode==200||responseCode==201) {
                datasource.deleteFileretrieval(fileId);
            }
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "fileretrieval", "stopped",reason));
            PreyLogger.d("Fileretrieval stopped");
        }catch(Exception e){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId, UtilJson.makeMapParam("start", "fileretrieval", "failed", e.getMessage()));
            PreyLogger.d("Fileretrieval failed:"+e.getMessage());
        }
    }
}
