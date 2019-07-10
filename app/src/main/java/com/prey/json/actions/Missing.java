/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.report.ReportService;
import com.prey.json.UtilJson;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.List;

public class Missing {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Missing start");
        String messageId = null;
        String reason = null;
        try {
            ctx.stopService(new Intent(ctx, ReportService.class));
        } catch (Exception e) {
            PreyLogger.e("stopService:" + e.getMessage(), e);
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "missing", "started", reason));
        try {
            PreyHttpResponse response = PreyWebServices.getInstance().setMissing(ctx, "true");
            if (response != null) {
                int code = response.getStatusCode();
                PreyLogger.d("Missing code:" + code);
                if (code == 200 || code == 201) {
                    new Report().get(ctx, list, parameters);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "missing", "stopped", reason));
                } else {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "missing", "failed", "code:" + code));
                }
            }
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "missing", "failed", e.getMessage()));
        }
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Missing stop");
        String messageId = null;
        String reason = null;
        try {
            ctx.stopService(new Intent(ctx, ReportService.class));
        } catch (Exception e) {
            PreyLogger.e("stopService:" + e.getMessage(), e);
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("stop", "missing", "started", reason));
        try {
            PreyHttpResponse response = PreyWebServices.getInstance().setMissing(ctx, "false");
            if (response != null) {
                int code = response.getStatusCode();
                PreyLogger.d("Missing code:" + code);
                if (code == 200 || code == 201) {
                    new Report().stop(ctx, list, parameters);
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("stop", "missing", "stopped", reason));
                } else {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "missing", "failed", "code:" + code));
                }
            }
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop", "missing", "failed", e.getMessage()));
        }
    }

}
