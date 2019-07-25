/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlertThread extends Thread {

    private Context ctx;
    private String description;
    private String messageId;
    private String jobId;

    public AlertThread(Context ctx, String description, String messageId, String jobId) {
        this.ctx = ctx;
        this.description = description;
        this.messageId = messageId;
        this.jobId = jobId;
    }

    public void run() {
        try {
            String NOTIFICATION_CHANNEL_ID = "10002";
            PreyLogger.d("started alert");
            PreyLogger.d("description:" + description);
            int notificationId = AlertConfig.getAlertConfig(ctx).getNotificationId();
            String CHANNEL_ID = "CHANNEL_ALERT_ID";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "prey_alert";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        name, NotificationManager.IMPORTANCE_HIGH);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
            String reason = null;
            if (jobId != null && !"".equals(jobId)) {
                reason = "{\"device_job_id\":\"" + jobId + "\"}";
            }
            PreyLogger.d("notificationId:" + notificationId);
            Intent buttonIntent2 = new Intent(ctx, AlertReceiver.class);
            buttonIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            buttonIntent2.setAction("" + notificationId);
            buttonIntent2.putExtra("notificationId", notificationId);
            buttonIntent2.putExtra("messageId", messageId);
            buttonIntent2.putExtra("reason", reason);
            PendingIntent btPendingIntent2 = PendingIntent.getBroadcast(ctx, 0, buttonIntent2, 0);
            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder notification = new Notification.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon2)
                        .setContentTitle(ctx.getString(R.string.title_alert))
                        .setStyle(new Notification.BigTextStyle().bigText(description))
                        .addAction(R.drawable.xx2, ctx.getString(R.string.close_alert), btPendingIntent2)
                        .setDeleteIntent(btPendingIntent2)
                        .setContentIntent(btPendingIntent2)
                        .setAutoCancel(true);
                notificationManager.notify(notificationId, notification.build());
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.icon2)
                        .setContentTitle(ctx.getString(R.string.title_alert))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                        .addAction(R.drawable.xx2, ctx.getString(R.string.close_alert), btPendingIntent2)
                        .setDeleteIntent(btPendingIntent2)
                        .setContentIntent(btPendingIntent2)
                        .setAutoCancel(true);
                notificationManager.notify(notificationId, builder.build());
            }
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "alert", "started", reason));
            PreyConfig.getPreyConfig(ctx).setNextAlert(true);
        } catch (Exception e) {
            PreyLogger.e("failed alert: " + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId, UtilJson.makeMapParam("start", "alert", "failed", e.getMessage()));
        }
    }

}
