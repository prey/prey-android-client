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
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Fileretrieval {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.i("Fileretrieval start");
        try {
            String path = parameters.getString("path");
            String name = parameters.getString("name");

            String file_id = parameters.getString("file_id");


            File file=new File(Environment.getExternalStorageDirectory()+"/"+path);

            PreyWebServices.getInstance().uploadFile(ctx,file,file_id);

        }catch(Exception e){

        }


    }
}
