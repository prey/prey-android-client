/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

public class AwareConfig {

    private static AwareConfig cachedInstance = null;
    private Context ctx;

    private AwareConfig(Context ctx) {
        this.ctx = ctx;
    }

    public static synchronized AwareConfig getAwareConfig(Context ctx) {
        if (cachedInstance == null) {
            synchronized (AwareConfig.class) {
                if (cachedInstance == null)
                    cachedInstance = new AwareConfig(ctx);
            }
        }
        return cachedInstance;
    }

    public boolean getLocationAware() {
        boolean locationAware = false;
        try {
            JSONObject jsnobject = PreyWebServices.getInstance().getStatus(ctx);
            if (jsnobject != null) {
                JSONObject jsnobjectSettings = jsnobject.getJSONObject("settings");
                JSONObject jsnobjectLocal = jsnobjectSettings.getJSONObject("local");
                locationAware = jsnobjectLocal.getBoolean("location_aware");
                PreyLogger.d("locationAware :" + locationAware);
            } else {
                PreyLogger.d("getLocationAware null");
            }
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(), e);
        }
        return locationAware;
    }

    public void init() {
        boolean locationAware = getLocationAware();
        if (locationAware) {
            startAware();
        }
    }

    public void startAware() {
        PreyLogger.d("startAware");
        PreyConfig.getPreyConfig(ctx).setAware(true);
        PreyConfig.getPreyConfig(ctx).setIntervalAware("15");
        AwareScheduled.getInstance(ctx).run();
    }
}