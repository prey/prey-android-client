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
import android.location.Location;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.geofences.GeofenceDto;
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
                int geofenceMaximumAccuracy=PreyConfig.getPreyConfig(ctx).getGeofenceMaximumAccuracy();
                PreyLocation locationNow = null;
                float accuracy=0;
                do {
                    locationNow = LocationUtil.getLocation(ctx, null, false);
                    if(locationNow!=null){
                        accuracy=locationNow.getAccuracy();
                    }
                    Thread.sleep(1000);
                    i = i + 1;
                } while (i < 3);


                if(locationNow != null && geofenceMaximumAccuracy<accuracy){
                    locationNow=null;
                }
                if (locationNow != null) {
                    PreyLocation locationOld = PreyConfig.getPreyConfig(ctx).getLocationAware();
                    if (locationOld != null) {
                        double distance = distance(locationNow, locationOld);
                        PreyLogger.d("distance:"+distance);
                        if (distance <= PreyConfig.getPreyConfig(ctx).getDistanceAware()){
                            locationNow=null;
                        }
                    }
                }
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
                    PreyConfig.getPreyConfig(ctx).setLocationAware(locationNow);
                    PreyHttpResponse preyResponse = PreyWebServices.getInstance().sendLocation(ctx, location);
                    if (preyResponse != null) {
                        if (preyResponse.getStatusCode() == 201) {
                            PreyLogger.d("getStatusCode 201");
                            PreyConfig.getPreyConfig(ctx).setAware(false);
                            PreyConfig.getPreyConfig(ctx).setIntervalAware("");
                            AwareScheduled.getInstance(ctx).reset();
                        }
                    }
                }
            }
        } catch (Exception e) {
            PreyLogger.e("error AwareService run:" + e.getMessage(), e);
        }
    }

    private double distance(PreyLocation start, PreyLocation end){
        Location locStart = new Location("");
        locStart.setLatitude(start.getLat());
        locStart.setLongitude(start.getLng());
        Location locEnd = new Location("");
        locEnd.setLatitude(end.getLat());
        locEnd.setLongitude(end.getLng());
        return Math.round(locStart.distanceTo(locEnd));
    }


}
