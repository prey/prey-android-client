/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.triggers.TriggerController;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.List;

public class Triggers {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("starting Trigger");
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:" + messageId);
        } catch (Exception e) {
        }
        String reason = null;
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "trigger", "started", reason));
        TriggerController.getInstance().run(ctx);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "trigger", "stopped", reason));
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("stop Trigger");
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
        }
        String reason = null;
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("stop", "trigger", "started", reason));
        TriggerController.getInstance().run(ctx);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("stop", "trigger", "stopped", reason));
    }
}