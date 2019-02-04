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

    public int getNotificationId(){
        notificationId+=1;
        return notificationId;
    }

}