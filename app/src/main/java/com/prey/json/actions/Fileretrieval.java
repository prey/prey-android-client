/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.os.Environment;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Fileretrieval {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            PreyLogger.d("Fileretrieval started");
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "fileretrieval", "started"));
            String path = parameters.getString("path");
            String fileId = parameters.getString("file_id");
            if(fileId==null||"".equals(fileId)||"null".equals(fileId)){
                throw new Exception("file_id null");
            }
            File file=new File(Environment.getExternalStorageDirectory()+"/"+path);
            PreyWebServices.getInstance().uploadFile(ctx, file, fileId);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "fileretrieval", "stopped"));
            PreyLogger.d("Fileretrieval stopped");
        }catch(Exception e){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "fileretrieval", "failed", e.getMessage()));
            PreyLogger.d("Fileretrieval failed:"+e.getMessage());
        }
    }
}
