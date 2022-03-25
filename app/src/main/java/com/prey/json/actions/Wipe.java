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

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.wipe.WipeThread;
import com.prey.json.UtilJson;

public class Wipe {

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        //execute(ctx, list, parameters);
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        execute(ctx, list, parameters);
    }

    public void execute(Context ctx, List<ActionResult> list, JSONObject parameters) {
        boolean wipe = false;
        boolean deleteSD = false;
        try {
            String sd = null;
            if (parameters != null && parameters.has("parameter")) {
                sd = parameters.getString("parameter");
                PreyLogger.d(String.format("sd:%s", sd));
            }
            if (sd != null && "sd".equals(sd)) {
                wipe = false;
                deleteSD = true;
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
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
        try {
            String factoryReset = UtilJson.getString(parameters, "factory_reset");
            PreyLogger.i(String.format("factoryReset:%s", factoryReset));
            if ("on".equals(factoryReset) || "y".equals(factoryReset) || "true".equals(factoryReset)) {
                wipe = true;
            }
            if ("off".equals(factoryReset) || "n".equals(factoryReset) || "false".equals(factoryReset)) {
                wipe = false;
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        try {
            String wipeSim = UtilJson.getString(parameters, "wipe_sim");
            PreyLogger.i(String.format("wipeSim:%s", wipeSim));
            if ("on".equals(wipeSim) || "y".equals(wipeSim) || "true".equals(wipeSim)) {
                deleteSD = true;
            }
            if ("off".equals(wipeSim) || "n".equals(wipeSim) || "false".equals(wipeSim)) {
                deleteSD = false;
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:", e.getMessage()), e);
        }
        PreyLogger.i(String.format("wipe:%b deleteSD%b:", wipe, deleteSD));
        new WipeThread(ctx, wipe, deleteSD, messageId,jobId).start();
    }
}