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
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
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
            String unlock = null;
            try {
                unlock = parameters.getString(PreyConfig.UNLOCK_PASS);
                PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
                PreyLogger.i("unlock:"+unlock);
            } catch (Exception e) {
            }
            Intent intent = new Intent(ctx, PreyLockService.class);
            ctx.startService(intent);
            PreyLogger.d("________startService PreyLockService");
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed",messageId, UtilJson.makeMapParam("start", "lock", "started",null));
        } catch (Exception e) {
            PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
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
            PreyConfig.getPreyConfig(ctx).setUnlockPass(null);
            Intent intent = new Intent(ctx, PreyLockService.class);
            PreyLogger.d("________stopService PreyLockService");
            ctx.stopService(intent);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "lock", "stopped",null));
        } catch (Exception e) {
            PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "lock", "failed", e.getMessage()));
        }
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try {
            String unlock = parameters.getString("parameter");
            lock(ctx, unlock,null);

        } catch (Exception e) {
            PreyLogger.i("Error causa:" + e.getMessage());
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "failed", e.getMessage()));
        }
    }

    public void lock(Context ctx, String unlock,String messageId) {
        if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {

            PreyConfig.getPreyConfig(ctx).setLock(true);
            FroyoSupport.getInstance(ctx).changePasswordAndLock(unlock, true);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed",messageId, UtilJson.makeMapParam("start", "lock", "started",null));
            PreyConfig.getPreyConfig(ctx).setLastEvent("lock_started");
        }
    }

}

