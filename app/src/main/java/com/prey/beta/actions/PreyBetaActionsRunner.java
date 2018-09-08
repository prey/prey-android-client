/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionsController;
import com.prey.beta.services.PreyBetaRunnerService;
import com.prey.exceptions.PreyException;
import com.prey.json.parser.JSONParser;
import com.prey.managers.PreyConnectivityManager;
import com.prey.managers.PreyTelephonyManager;
import com.prey.net.PreyWebServices;

public class PreyBetaActionsRunner implements Runnable {

    private Context ctx;

    private String cmd;
    private String messageId;

    public PreyBetaActionsRunner(Context context, String cmd) {
        this.ctx = context;
        this.cmd = cmd;
    }

    public void run() {
        execute();
    }

    public void execute() {

        if (PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey(true)) {
            PreyTelephonyManager preyTelephony = PreyTelephonyManager.getInstance(ctx);
            PreyConnectivityManager preyConnectivity = PreyConnectivityManager.getInstance(ctx);
            boolean connection = false;
            try {
                List<JSONObject> jsonObject = null;

                connection = preyTelephony.isDataConnectivityEnabled() || preyConnectivity.isConnected();

                if (connection) {
                    try {
                        if (cmd == null || "".equals(cmd)) {
                            jsonObject = PreyBetaActionsRunner.getInstructions(ctx);
                        } else {
                            jsonObject = getInstructionsNewThread(ctx, cmd);
                        }
                    } catch (Exception e) {
                    }
                    if (jsonObject == null || jsonObject.size() == 0) {
                        PreyLogger.d("nothing");
                    } else {
                        PreyLogger.d("runInstructions");
                        runInstructions(jsonObject);
                    }
                }
            } catch (Exception e) {
                PreyLogger.e("Error, because:" + e.getMessage(), e);
            }
            PreyLogger.d("Prey execution has finished!!");

        }
        ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
    }

    private static List<JSONObject> getInstructionsNewThread(Context ctx, String cmd) throws PreyException {
        List<JSONObject> jsonObject = new JSONParser().getJSONFromTxt(ctx, "[" + cmd + "]");
        final Context context = ctx;
        new Thread(new Runnable() {
            public void run() {
                try {
                    PreyLogger.d("_________New Thread");
                    PreyBetaActionsRunner.getInstructions(context);
                } catch (PreyException e) {
                    PreyLogger.e("_________getInstructionsNewThread:"+e.getMessage(),e);
                }
            }
        }).start();
        return jsonObject;
    }

    private static List<JSONObject> getInstructions(Context ctx) throws PreyException {
        PreyLogger.d("______________________________");
        PreyLogger.d("_______getInstructions________");
        List<JSONObject> jsonObject = null;
        try {
            jsonObject = PreyWebServices.getInstance().getActionsJsonToPerform(ctx);
        } catch (PreyException e) {
            PreyLogger.e("Exception getting device's xml instruction set", e);
            throw e;
        }
        return jsonObject;
    }

    private List<HttpDataService> runInstructions(List<JSONObject> jsonObject) throws PreyException {
        List<HttpDataService> listData = null;
        listData = ActionsController.getInstance(ctx).runActionJson(ctx, jsonObject);
        return listData;
    }

}

