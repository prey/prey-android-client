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
import com.prey.actions.HttpDataService;
import com.prey.actions.aware.AwareController;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
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
        PreyConfig.getPreyConfig(ctx).setLocation(null);
        PreyConfig.getPreyConfig(ctx).setLocationInfo("");
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("get", "location", "started",reason));
        PreyLogger.d(this.getClass().getName());
        int i = 0;
        int maximum = LocationUtil.MAXIMUM_OF_ATTEMPTS;
        HttpDataService data = null;
        ArrayList<HttpDataService> dataToBeSent = null;
        float accuracy = -1;
        boolean send;
        boolean first = true;
        while (i < maximum) {
            send = false;
            try {
                LocationUtil.dataLocation(ctx, messageId, true);
                PreyLocation location = PreyConfig.getPreyConfig(ctx).getLocation();
                if (location != null) {
                    data = LocationUtil.convertData(location);
                    String acc = data.getDataListKey(LocationUtil.ACC);
                    if (acc != null && !acc.equals("")) {
                        float newAccuracy = 0;
                        try {
                            newAccuracy = Float.parseFloat(acc);
                            PreyLogger.d(String.format("accuracy_:%s newAccuracy:%s", accuracy, newAccuracy));
                        } catch (Exception e) {
                            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
                        }
                        if (newAccuracy > 0) {
                            if (accuracy == -1 || accuracy > newAccuracy) {
                                send = true;
                                accuracy = newAccuracy;
                            }
                        }
                    }
                    if (send) {
                        //It is added if it is the first time the location is sent
                        HttpDataService dataToast = new HttpDataService("skip_toast");
                        dataToast.setList(false);
                        dataToast.setKey("skip_toast");
                        dataToast.setSingleData(Boolean.toString(!first));
                        dataToBeSent = new ArrayList<HttpDataService>();
                        dataToBeSent.add(data);
                        dataToBeSent.add(dataToast);
                        PreyLogger.d(String.format("send [%s]:%s", i, accuracy));
                        PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
                        first = false;
                        i = LocationUtil.MAXIMUM_OF_ATTEMPTS;
                    }
                }
                if (i < maximum) {
                    try {
                        Thread.sleep(LocationUtil.SLEEP_OF_ATTEMPTS[i] * 1000);
                    } catch (Exception e) {
                        i = LocationUtil.MAXIMUM_OF_ATTEMPTS;
                        break;
                    }
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