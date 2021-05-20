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

import android.content.Context;

public class Camouflage {

    public static void hide(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        PreyLogger.d("started hide");
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        String reason = null;
        try {
            String jobId = parameters.getString(PreyConfig.JOB_ID);
            PreyLogger.d("jobId:"+jobId);
            if(jobId!=null&&!"".equals(jobId)){
                reason="{\"device_job_id\":\""+jobId+"\"}";
            }
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "camouflage", "started",reason));
        PreyConfig.getPreyConfig(ctx).setCamouflageSet(true);
        PreyLogger.d("stopped hide");
        PreyConfig.getPreyConfig(ctx).setLastEvent("camouflage_hide");
    }

    public static void unhide(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        PreyLogger.d("started unhide");
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        String reason = null;
        try {
            String jobId = parameters.getString(PreyConfig.JOB_ID);
            PreyLogger.d("jobId:"+jobId);
            if(jobId!=null&&!"".equals(jobId)){
                reason="{\"device_job_id\":\""+jobId+"\"}";
            }
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "camouflage", "stopped",reason));
        PreyConfig.getPreyConfig(ctx).setCamouflageSet(false);
        PreyLogger.d("stopped unhide");
        PreyConfig.getPreyConfig(ctx).setLastEvent("camouflage_unhide");
    }

}