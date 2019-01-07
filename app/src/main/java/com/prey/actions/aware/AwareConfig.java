/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

public class AwareConfig {

    private static AwareConfig cachedInstance = null;
    private Context ctx;
    private int notificationId=1;

    private AwareConfig(Context ctx) {
        this.ctx = ctx;
        locationAware=getLocationAwareJson();
    }

    public boolean locationAware = false;

    public static synchronized AwareConfig getAwareConfig(Context ctx) {
        if (cachedInstance == null) {
            synchronized (AwareConfig.class) {
                if (cachedInstance == null)
                    cachedInstance = new AwareConfig(ctx);
            }
        }
        return cachedInstance;
    }
    public int getNotificationId(){
        notificationId+=1;
        return notificationId;
    }

    public boolean isLocationAware(){
        return locationAware;
    }

    public void setLocationAware(boolean locationAware){
        this.locationAware=locationAware;
    }

    private boolean getLocationAwareJson() {
        try {
            JSONObject jsnobject = PreyWebServices.getInstance().getStatus(ctx);
            if (jsnobject != null) {
                PreyLogger.d("AWARE jsnobject :" + jsnobject);
                JSONObject jsnobjectSettings = jsnobject.getJSONObject("settings");
                JSONObject jsnobjectLocal = jsnobjectSettings.getJSONObject("local");
                locationAware = jsnobjectLocal.getBoolean("location_aware");
                PreyLogger.d("AWARE getLocationAware :" + locationAware);
            } else {
                PreyLogger.d("AWARE getLocationAware null");
                locationAware = false;
            }
        } catch (Exception e) {
            PreyLogger.e("AWARE Error:" + e.getMessage(), e);
            locationAware = false;
        }
        return locationAware;
    }

}