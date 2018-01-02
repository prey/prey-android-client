/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.content.Context;

public class WipeThread extends Thread {

    private Context ctx;
    private boolean wipe;
    private boolean deleteSD;
    private String jobId;

    public WipeThread(Context ctx,boolean wipe,boolean deleteSD, String messageId,String jobId) {
        this.ctx = ctx;
        this.deleteSD = deleteSD;
        this.wipe = wipe;
        String messageId1 = messageId;
        this.jobId = jobId;
    }

    public void run() {
        String reason=null;
        if(jobId!=null&&!"".equals(jobId)){
            reason="{\"device_job_id\":\""+jobId+"\"}";
        }
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","started",reason));
        try{
            if(deleteSD){
                WipeUtil.deleteSD();
                if(!wipe){
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","stopped",reason));
                }
            }
        }catch(Exception e){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","failed",e.getMessage()));
            PreyLogger.e("Error Wipe:"+e.getMessage(), e);
        }
        try{
            if (wipe&&preyConfig.isFroyoOrAbove()){
                PreyLogger.d("Wiping the device!!");
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","stopped",reason));
                FroyoSupport.getInstance(ctx).wipe();
            }
        }catch(Exception e){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","failed",e.getMessage()));
            PreyLogger.e("Error Wipe:"+e.getMessage(), e);
        }
    }

}
