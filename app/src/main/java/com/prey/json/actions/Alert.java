/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.HttpDataService;
import com.prey.actions.alert.AlertThread;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.content.Context;

public class Alert extends JsonAction {

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String alert = null;
        try {
            alert = parameters.getString("parameter");
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        startAlert(ctx, alert,null,null,false);
    }

    /**
     * Method cancel
     *
     * @param ctx
     * @param list
     * @param parameters
     */
    public void cancel(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
            PreyLogger.d(String.format("Error:%s", e.getMessage()));
        }
        try {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("cancel", "alert", "stopped", null));
        } catch (Exception e) {
            PreyLogger.d(String.format("Error:%s", e.getMessage()));
        }
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String alert = "";
        try {
            alert = UtilJson.getString(parameters, "alert_message");
        } catch (Exception e) {
            try {
                alert = UtilJson.getString(parameters, "message");
            } catch (Exception e2) {
                PreyLogger.e(String.format("Error:%s", e2.getMessage()), e2);
            }
        }
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
            PreyLogger.d(String.format("messageId:%s", messageId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        String jobId = null;
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID);
            PreyLogger.d(String.format("jobId:%s", jobId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        boolean fullscreen_notification = false;
        try {
            fullscreen_notification = UtilJson.getBoolean(parameters, "fullscreen_notification");
            PreyLogger.d(String.format("fullscreen_notification:%s", fullscreen_notification));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        if (!PreyPermission.areNotificationsEnabled(ctx)) {
            fullscreen_notification = true;
        }
        startAlert(ctx, alert, messageId, jobId, fullscreen_notification);
    }

    public void startAlert(Context ctx, String alert, String messageId,String jobId,boolean fullscreen_notification) {
        try {
            if (alert != null && !"".equals(alert)) {
                new AlertThread(ctx, alert, messageId,jobId,fullscreen_notification).start();
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error, causa:%s" , e.getMessage()), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "alert", "failed", e.getMessage()));
        }
    }

}