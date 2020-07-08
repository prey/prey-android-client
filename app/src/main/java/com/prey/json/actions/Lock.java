/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.WindowManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.LoginActivity;
import com.prey.activities.PasswordActivity2;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.exceptions.PreyException;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.receivers.PreyDeviceAdmin;
import com.prey.services.PreyDisablePowerOptionsService;
import com.prey.services.PreyLockService;
import com.prey.services.PreySecureService;

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
            String jobId =null;
            try {
                jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                reason="{\"device_job_id\":\""+jobId+"\"}";
                PreyConfig.getPreyConfig(ctx).setJobIdLock(jobId);
            } catch (Exception e) {
            }
            String unlock = null;
            try {
                unlock = parameters.getString(PreyConfig.UNLOCK_PASS);
                PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
            } catch (Exception e) {
            }
            lock(ctx, unlock, messageId, reason,jobId);
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
            String reason="{\"origin\":\"panel\"}";;
            try {
                String jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
                reason="{\"device_job_id\":\""+jobId+"\",\"origin\":\"panel\"}";

            } catch (Exception e) {
            }
            String jobIdLock=PreyConfig.getPreyConfig(ctx).getJobIdLock();
            if(jobIdLock!=null&&!"".equals(jobIdLock)){
                reason="{\"device_job_id\":\""+jobIdLock+"\",\"origin\":\"panel\"}";
                PreyConfig.getPreyConfig(ctx).setJobIdLock("");
            }
            PreyConfig.getPreyConfig(ctx).setLock(false);
            PreyConfig.getPreyConfig(ctx).deleteUnlockPass();
            try{ctx.stopService(new Intent(ctx, PreySecureService.class));}catch(Exception e){}
            Intent intent = new Intent(ctx, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(ctx)) {
                Thread.sleep(1000);
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed",messageId,UtilJson.makeMapParam("start", "lock", "stopped",reason));
                Thread.sleep(2000);
                View view=PreyConfig.getPreyConfig(ctx).view;
                if(view!=null){
                    WindowManager wm = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
                    wm.removeView(view);
                }
            }else{
                PreyLogger.d("-- Unlock instruction received");
                try{
                    if(!PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() ) {
                        FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
                        @SuppressLint("InvalidWakeLockTag") WakeLock screenLock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, PreyConfig.TAG);
                        screenLock.acquire();
                        screenLock.release();
                    }
                    Thread.sleep(2000);
                    reason="{\"origin\":\"panel\"}";
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped",reason));
                }catch(Exception e){
                    throw new PreyException(e);
                }
            }
            ctx.sendBroadcast(new Intent(CheckPasswordHtmlActivity.CLOSE_PREY));
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage() + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String unlock = parameters.getString("parameter");
            lock(ctx, unlock, null, null,null);
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(),e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void lock(final Context ctx, String unlock,final String messageId,final String reason,String device_job_id) {
        PreyLogger.d("lock unlock:"+unlock+" messageId:"+ messageId+" reason:"+reason);
        PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
        PreyConfig.getPreyConfig(ctx).setLock(true);
        if(PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() ) {
            if(PreyPermission.canDrawOverlays(ctx)) {
                Intent intent = new Intent(ctx, PreyLockService.class);
                ctx.startService(intent);
                Intent intent4 = new Intent(ctx, PasswordActivity2.class);
                intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent4);
                if (PreyConfig.getPreyConfig(ctx).isDisablePowerOptions()) {
                    Intent intent2 = new Intent(ctx, PreyDisablePowerOptionsService.class);
                    ctx.startService(intent2);
                }
            }else{
                if(PreyPermission.isAccessibilityServiceEnabled(ctx)) {
                    Intent intent4 = new Intent(ctx, PasswordActivity2.class);
                    intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent4);
                }else{
                    PreyDeviceAdmin.lockWhenYouNocantDrawOverlays(ctx);
                }
            }
        }else{
            PreyDeviceAdmin.lockOld(ctx);
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "lock", "started", reason));
                }catch(Exception e){}
            }
        }).start();
    }

}

