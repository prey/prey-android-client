/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.json.UtilJson;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

public class AwareService extends IntentService {

    public AwareService() {
        super("awareService");
    }

    public AwareService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        run(getApplicationContext());
        stopSelf();
    }

    public void run(Context ctx) {
        PreyLogger.d("AwareService run");
        int i = 0;
        try {
            String minuteSt = PreyConfig.getPreyConfig(ctx).getIntervalAware();
            PreyLogger.d("AwareService [" + minuteSt + "]");
            if (PreyConfig.getPreyConfig(ctx).getAware() && minuteSt != null && !"".equals(minuteSt)) {

                PreyLocation locationNow = null;
                do {
                    locationNow = LocationUtil.getLocation(ctx, null, false);
                    //PreyLogger.d("locationNow lat:" + locationNow.getLat() + " lng:" + locationNow.getLng() + " acc:" + locationNow.getAccuracy());
                    Thread.sleep(1000);
                    i = i + 1;
                } while (i < 3);
                if (locationNow != null) {
                    String messageId = null;
                    String reason = null;
                    double accD = Math.round(locationNow.getAccuracy() * 100.0) / 100.0;
                    JSONObject json = new JSONObject();
                    json.put("lat", Double.toString(locationNow.getLat()));
                    json.put("lng", Double.toString(locationNow.getLng()));
                    json.put("accuracy", Double.toString(accD));
                    json.put("method", locationNow.getMethod());
                    JSONObject location = new JSONObject();
                    location.put("location", json);
                    PreyHttpResponse preyResponse = PreyWebServices.getInstance().sendLocation(ctx, location);
                    if (preyResponse != null) {
                        if (preyResponse.getStatusCode() == 201) {
                            PreyLogger.d("getStatusCode 201");
                            PreyConfig.getPreyConfig(ctx).setAware(false);
                            PreyConfig.getPreyConfig(ctx).setIntervalAware("");
                            AwareScheduled.getInstance(ctx).reset();
                            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("stop", "aware", "stopped", reason));
                        }
                        if (preyResponse.getStatusCode() == 200) {
                            PreyLogger.d("getStatusCode 200");
                            //PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "aware", "started", reason));
                        }
                    }
                }
            }
        } catch (Exception e) {
            PreyLogger.e("error AwareService run:" + e.getMessage(), e);
        }
    }
}
