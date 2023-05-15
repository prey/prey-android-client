/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.provider.Settings;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.actions.HttpDataService;
import com.prey.actions.aware.AwareConfig;
import com.prey.actions.aware.AwareController;
import com.prey.actions.location.LocationThread;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocationManager;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class Location extends JsonAction{

    public static final String DATA_ID = "geo";

    public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Ejecuting Location Report.");
        PreyLocationManager.getInstance(ctx).setLastLocation(null);
        List<HttpDataService> listResult=super.report(ctx, list, parameters);
        return listResult;
    }

    public  List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Ejecuting Location Get.");
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
            PreyLogger.e(String.format("Error:%s" + e.getMessage()), e);
        }
        String reason = null;
        if (jobId != null && !"".equals(jobId)) {
            reason = "{\"device_job_id\":\"" + jobId + "\"}";
        }
        PreyLocationManager.getInstance(ctx).setLastLocation(null);
        PreyConfig.getPreyConfig(ctx).setLocationInfo("");
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("get", "location", "started",reason));
        PreyLogger.d(this.getClass().getName());
        int i = 0;
        int maximum = LocationUtil.MAXIMUM_OF_ATTEMPTS;
        HttpDataService data = null;
        ArrayList<HttpDataService> dataToBeSent = null;
        float accuracy = -1;
        boolean enviar;
        while (i < maximum) {
            enviar = false;
            try {
                data = LocationUtil.dataLocation(ctx, messageId, true);
                dataToBeSent = new ArrayList<HttpDataService>();
                dataToBeSent.add(data);
                String acc = data.getDataListKey(LocationUtil.ACC);
                if (acc != null && !acc.equals("")) {
                    float newAccuracy = 0;
                    try {
                        newAccuracy = Float.parseFloat(acc);
                        PreyLogger.d(String.format("accuracy_:%s newAccuracy:%s", accuracy, newAccuracy));
                    } catch (Exception e) {
                    }
                    if (newAccuracy > 0) {
                        if (accuracy == -1 || accuracy > newAccuracy) {
                            enviar = true;
                            accuracy = newAccuracy;
                        }
                    }
                }
                if (enviar) {
                    PreyLogger.d(String.format("enviar [%s]:%s", i, accuracy));
                    PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
                }
                try {
                    Thread.sleep(LocationUtil.SLEEP_OF_ATTEMPTS[i] * 1000);
                } catch (Exception e) {
                    i = LocationUtil.MAXIMUM_OF_ATTEMPTS;
                    break;
                }
            } catch (Exception e) {
                i = LocationUtil.MAXIMUM_OF_ATTEMPTS;
                break;
            }
            i++;
        }
        if (data==null){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"failed", messageId, UtilJson.makeMapParam("get", "location", "failed",PreyConfig.getPreyConfig(ctx).getLocationInfo()));
        }else{
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("get", "location", "stopped",reason));
        }
        try {
            String nameDevice = Settings.Secure.getString(ctx.getContentResolver(), "bluetooth_name");
            if (nameDevice != null && !"".equals(nameDevice)) {
                PreyLogger.d(String.format("nameDevice: %s", nameDevice));
                PreyWebServices.getInstance().sendPreyHttpDataName(ctx, nameDevice);
                String nameDeviceInfo = PreyWebServices.getInstance().getNameDevice(ctx);
                if (nameDeviceInfo != null && !"".equals(nameDeviceInfo)) {
                    PreyLogger.d(String.format("nameDeviceInfo: %s", nameDeviceInfo));
                    PreyConfig.getPreyConfig(ctx).setDeviceName(nameDeviceInfo);
                }
            }
        } catch (Exception e) {
            PreyLogger.d(String.format("Data wasn't send: %s", e.getMessage()));
        }
        return dataToBeSent;
    }

    public  List<HttpDataService> start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Ejecuting Location Start.");
        List<HttpDataService> listResult=super.get(ctx, list, parameters);
        return listResult;
    }

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters){
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
            PreyLogger.d(String.format("messageId:%s", messageId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        HttpDataService data = LocationUtil.dataLocation(ctx, messageId, false);
        return data;
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
    }

    public  List<HttpDataService> start_location_aware(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("AWARE start_location_aware:");
        AwareController.getInstance().init(ctx);
        return null;
    }

}