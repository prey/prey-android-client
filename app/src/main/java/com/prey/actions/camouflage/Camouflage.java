/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.camouflage;

import java.util.List;

import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class Camouflage {

    public static void hide(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        PreyLogger.i("started hide");
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "camouflage", "started",null));
        PreyConfig.getPreyConfig(ctx).setCamouflageSet(true);

        ComponentName componentToDisabled = new ComponentName("com.prey", "com.prey.activities.LoginActivity");
        PackageManager pm = ctx.getPackageManager();
        pm.setComponentEnabledSetting(componentToDisabled, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        PreyLogger.i("stopped hide");
        PreyConfig.getPreyConfig(ctx).setLastEvent("camouflage_hide");
    }

    public static void unhide(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        PreyLogger.i("started unhide");
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "camouflage", "stopped",null));
        PreyConfig.getPreyConfig(ctx).setCamouflageSet(false);

        ComponentName componentToEnabled = new ComponentName("com.prey", "com.prey.activities.LoginActivity");
        PackageManager pm = ctx.getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(componentToEnabled, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        PreyLogger.i("stopped unhide");
        PreyConfig.getPreyConfig(ctx).setLastEvent("camouflage_unhide");
    }
}

