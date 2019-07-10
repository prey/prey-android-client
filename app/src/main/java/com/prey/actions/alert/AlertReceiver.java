/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", 0);
        final String messageId = intent.getStringExtra("messageId");
        final String reason = intent.getStringExtra("reason");
        PreyLogger.d("notificationId:" + notificationId);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
        new Thread() {
            public void run() {
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(context, "processed", messageId, UtilJson.makeMapParam("start", "alert", "stopped", reason));
            }
        }.start();
    }
}
