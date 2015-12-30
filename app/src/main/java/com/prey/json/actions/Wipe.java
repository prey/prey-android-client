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

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.wipe.WipeThread;

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
            String sd = parameters.getString("parameter");
            PreyLogger.i("sd:" + sd);
            if (sd != null && "sd".equals(sd)) {
                wipe = false;
                deleteSD = true;
            }
        } catch (Exception e) {

        }
        try {
            String factoryReset = parameters.getString("factory_reset");
            PreyLogger.i("factoryReset:" + factoryReset);
            if ("on".equals(factoryReset) || "y".equals(factoryReset) || "true".equals(factoryReset)) {
                wipe = true;
            }
            if ("off".equals(factoryReset) || "n".equals(factoryReset) || "false".equals(factoryReset)) {
                wipe = false;
            }
        } catch (Exception e) {

        }
        try {
            String wipeSim = parameters.getString("wipe_sim");
            PreyLogger.i("wipeSim:" + wipeSim);
            if ("on".equals(wipeSim) || "y".equals(wipeSim) || "true".equals(wipeSim)) {
                deleteSD = true;
            }
            if ("off".equals(wipeSim) || "n".equals(wipeSim) || "false".equals(wipeSim)) {
                deleteSD = false;
            }
        } catch (Exception e) {

        }
        PreyLogger.i("wipe:" + wipe + " deleteSD:" + deleteSD);
        new WipeThread(ctx, wipe, deleteSD).start();
    }
}

