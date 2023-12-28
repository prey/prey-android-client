/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.logger.LoggerDatasource;
import com.prey.actions.logger.LoggerDto;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.List;

public class Logretrieval {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        get(ctx, list, parameters);
    }

    /**
     * Method retrieves the last logger
     *
     * @param ctx
     * @param list
     * @param parameters
     */
    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
            PreyLogger.d(String.format("messageId:%s", messageId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        String reason = null;
        LoggerDatasource datasource = new LoggerDatasource(ctx);
        List<LoggerDto> loggers = datasource.getAllLogger();
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("get", "logretrieval", "started", reason));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; loggers != null && i < loggers.size(); i++) {
            LoggerDto dto = loggers.get(i);
            if (!dto.getTxt().contains("[]")) {
                sb.append("info").append(dto.getTime()).append("[").append(dto.getType()).append("]:").append(dto.getTxt()).append("\n");
            }
        }
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("get", "logretrieval", "stopped", reason));
        try {
            PreyWebServices.getInstance().uploadLogger(ctx, sb.toString());
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "failed", messageId, UtilJson.makeMapParam("start", "logretrieval", "failed", e.toString()));
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
    }

}