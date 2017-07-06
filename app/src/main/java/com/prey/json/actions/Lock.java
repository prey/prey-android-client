/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.exceptions.PreyException;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.services.PreyLockService;

public class Lock extends JsonAction {

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }


    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String messageId = null;
            try {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID);
                PreyLogger.d("messageId:"+messageId);
            } catch (Exception e) {
            }
            String reason=null;
            try {
                String jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                reason="{\"device_job_id\":\""+jobId+"\"}";
            } catch (Exception e) {
            }
            String unlock = null;
            try {
                unlock = parameters.getString(PreyConfig.UNLOCK_PASS);
                PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
            } catch (Exception e) {
            }
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "lock", "started", reason));
            if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(ctx)) {
                Intent intent = new Intent(ctx, PreyLockService.class);
                ctx.startService(intent);
            }else{
                lock(ctx, unlock, messageId,reason);
            }
           // PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "lock", "stopped", reason));
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
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
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "lock", "started",reason));
            if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(ctx)) {
                PreyConfig.getPreyConfig(ctx).deleteUnlockPass();
                Intent intent = new Intent(ctx, PreyLockService.class);
                ctx.stopService(intent);
            }else{
                PreyLogger.d("-- Unlock instruction received");
                FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
                WakeLock screenLock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, PreyConfig.TAG);
                screenLock.acquire();
                screenLock.release();
            }
            //PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "lock", "stopped",reason));
            PreyConfig.getPreyConfig(ctx).setLastEvent("lock_stopped");
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "lock", "failed", e.getMessage()));
        }
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String unlock = parameters.getString("parameter");
            PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
            if(PreyConfig.getPreyConfig(ctx).isNougatOrAbove() && PreyPermission.canDrawOverlays(ctx)) {
                Intent intent = new Intent(ctx, PreyLockService.class);
                ctx.startService(intent);
            }else{
                lock(ctx, unlock, null,null);
            }
        } catch (Exception e) {
            PreyLogger.i("Error:" + e.getMessage());
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void lock(Context ctx, String unlock,String messageId,String reason) throws PreyException{
        if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {
            PreyConfig.getPreyConfig(ctx).setLock(true);
            FroyoSupport.getInstance(ctx).changePasswordAndLock(unlock, true);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed",messageId, UtilJson.makeMapParam("start", "lock", "started",reason));
            PreyConfig.getPreyConfig(ctx).setLastEvent("lock_started");
        }
    }

}

