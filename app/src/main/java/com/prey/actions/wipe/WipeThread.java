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
    private String messageId;

    public WipeThread(Context ctx,boolean wipe,boolean deleteSD, String messageId) {
        this.ctx = ctx;
        this.deleteSD = deleteSD;
        this.wipe = wipe;
        this.messageId = messageId;
    }

    public void run() {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","started",null));
        try{
            if(deleteSD){
                WipeUtil.deleteSD();
                if(!wipe){
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","stopped",null));
                }
            }
        }catch(Exception e){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","failed",e.getMessage()));
            PreyLogger.e("Error Wipe:"+e.getMessage(), e);
        }
        try{
            if (wipe&&preyConfig.isFroyoOrAbove()){
                PreyLogger.d("Wiping the device!!");
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","stopped",null));
                FroyoSupport.getInstance(ctx).wipe();
            }
        }catch(Exception e){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","wipe","failed",e.getMessage()));
            PreyLogger.e("Error Wipe:"+e.getMessage(), e);
        }
    }

}
