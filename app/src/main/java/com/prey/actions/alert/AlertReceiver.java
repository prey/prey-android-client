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
import com.prey.activities.PopUpAlertActivity;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", 0);
        final String messageId = intent.getStringExtra("messageId");
        final String reason = intent.getStringExtra("reason");
        PreyLogger.d("AlertReceiver notificationId:" + notificationId);
        String popupIntent = PopUpAlertActivity.POPUP_PREY + "_" + notificationId;
        PreyLogger.d("AlertReceiver popup intent:" + popupIntent);
        context.sendBroadcast(new Intent(popupIntent));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        // Tell the system to keep this receiver active while we POST the
        // "stopped" notify. Without this, onReceive returns immediately, the
        // popup activity finishes, and the process drops to "cached" — at
        // which point Android (and aggressive OEM battery managers) can kill
        // it BEFORE the worker thread finishes the HTTP request, silently
        // losing the action_stopped event on the server side.
        //
        // The worker uses sendNotifyActionResultPreyHttpSync (instead of the
        // default async variant) so we can call result.finish() only after
        // the POST has actually completed. If we used the async variant the
        // post would still race the OOM killer.
        final PendingResult pendingResult = goAsync();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttpSync(
                            context, "processed", messageId,
                            UtilJson.makeMapParam("start", "alert", "stopped", reason));
                } finally {
                    pendingResult.finish();
                }
            }
        }, "prey-alert-stopped-notify");
        worker.start();
    }
}
