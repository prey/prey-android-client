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

import com.prey.PreyConfig;
import com.prey.PreyLogger;
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
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        String jobId = null;
        try {
            jobId = parameters.getString(PreyConfig.JOB_ID);
            PreyLogger.d("jobId:"+jobId);
        } catch (Exception e) {
        }
        String reason=null;
        if(jobId!=null&&!"".equals(jobId)){
            reason="{\"device_job_id\":\""+jobId+"\"}";
        }
        PreyLocationManager.getInstance(ctx).setLastLocation(null);
        PreyConfig.getPreyConfig(ctx).setLocationInfo("");
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("get", "location", "started",reason));


        PreyLogger.d(this.getClass().getName());
        HttpDataService data = LocationUtil.dataLocation(ctx,messageId,true);
        if (data==null){
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"failed", messageId, UtilJson.makeMapParam("get", "location", "failed",PreyConfig.getPreyConfig(ctx).getLocationInfo()));
        }
        ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
        dataToBeSent.add(data);
        PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
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
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        HttpDataService data = LocationUtil.dataLocation(ctx,messageId,false);
        return data;
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        /*PreyLogger.d("Ejecuting sms Location Data.");
        String phoneNumber = null;
        if (parameters != null) {
            try {
                phoneNumber = parameters.getString("parameter");
            } catch (Exception e) {
                try {
                    phoneNumber = parameters.getString("phoneNumber");
                } catch (Exception e1) {
                    PreyLogger.e("Error, causa:" + e1.getMessage(), e1);
                }
            }
            try {
                if (phoneNumber != null && !"".equals(phoneNumber)) {
                    new LocationThread(ctx, phoneNumber).start();
                }
            } catch (Exception e) {
                PreyLogger.e("Error, causa:" + e.getMessage(), e);
            }
        }*/
    }

    public  List<HttpDataService> start_location_aware(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("AWARE start_location_aware:");
        AwareController.getInstance().init(ctx);
        return null;
    }

}

