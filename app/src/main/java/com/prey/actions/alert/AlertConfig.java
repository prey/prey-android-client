/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert;

import android.content.Context;

public class AlertConfig {

    private static AlertConfig cachedInstance = null;
    private Context ctx;
    private int notificationId = 100;

    private AlertConfig(Context ctx) {
        this.ctx = ctx;
    }

    public static synchronized AlertConfig getAlertConfig(Context ctx) {
        if (cachedInstance == null) {
            synchronized (AlertConfig.class) {
                if (cachedInstance == null)
                    cachedInstance = new AlertConfig(ctx);
            }
        }
        return cachedInstance;
    }

    public int getNotificationId() {
        notificationId = notificationId + 1;
        return notificationId;
    }

}