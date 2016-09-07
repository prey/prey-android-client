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
        }
        startAlert(ctx, alert,null);
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String alert = "";
        try {
            alert = parameters.getString("alert_message");
        } catch (Exception e) {
            try {
                alert = parameters.getString("message");
            } catch (Exception e2) {
            }
        }
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        startAlert(ctx, alert,messageId);
    }

    public void startAlert(Context ctx, String alert, String messageId) {
        try {
            if (alert != null && !"".equals(alert)) {
                new AlertThread(ctx, alert, messageId).start();
            }
        } catch (Exception e) {
            PreyLogger.e("Error, causa:" + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "alert", "failed", e.getMessage()));
        }
    }
}

