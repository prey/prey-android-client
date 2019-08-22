/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.PopUpAlertActivity;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class AlertThread extends Thread {

    private Context ctx;
    private String description;
    private String messageId;
    private String jobId;
    private boolean fullscreen_notification=false;

    public AlertThread(Context ctx, String description, String messageId, String jobId,boolean fullscreen_notification) {
        this.ctx = ctx;
        this.description = description;
        this.messageId = messageId;
        this.jobId = jobId;
        this.fullscreen_notification = fullscreen_notification;
    }

    public void run() {
        final int notificationId = AlertConfig.getAlertConfig(ctx).getNotificationId();
        if (PreyUtils.isChromebook(ctx)) {
            new Thread() {
                public void run() {
                    fullscreen(notificationId);
                }
            }.start();
        }else {
            if (fullscreen_notification) {
                new Thread() {
                    public void run() {
                        fullscreen(notificationId);
                    }
                }.start();
            }
            new Thread() {
                public void run() {
                    notification(notificationId);
                }
            }.start();
        }
    }

    public void notification(int notificationId) {
        try {
            String NOTIFICATION_CHANNEL_ID = "10002";
            PreyLogger.d("started alert");
            PreyLogger.d("description:" + description);

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
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.icon2)
                        .setContentTitle(ctx.getString(R.string.title_alert))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                        .addAction(R.drawable.xx2, ctx.getString(R.string.close_alert), btPendingIntent2)
                        .setDeleteIntent(btPendingIntent2)
                        .setContentIntent(btPendingIntent2)
                        .setAutoCancel(true);
                notificationManager.notify(notificationId, builder.build());
            }else {
                RemoteViews contentViewBig =null;
                if(description.length()<=70) {
                    PreyLogger.d("custom_notification1 length:"+description.length());
                    contentViewBig = new RemoteViews(ctx.getPackageName(), R.layout.custom_notification1);
                }else {
                    if (description.length() <= 170) {
                        PreyLogger.d("custom_notification2 length:"+description.length());
                        contentViewBig = new RemoteViews(ctx.getPackageName(), R.layout.custom_notification2);
                    } else {
                        PreyLogger.d("custom_notification3 length:"+description.length());
                        contentViewBig = new RemoteViews(ctx.getPackageName(), R.layout.custom_notification3);
                    }
                }
                RemoteViews contentViewSmall = new RemoteViews(ctx.getPackageName(),R.layout.custom_notification_small);
                contentViewBig.setOnClickPendingIntent(R.id.noti_button, btPendingIntent2);
                String regularBold= "fonts/Regular/regular-bold.otf";
                String regularBook= "fonts/Regular/regular-book.otf";
                String title_alert=ctx.getString(R.string.title_alert);
                PreyLogger.d("title_alert:"+title_alert);
                SpannableStringBuilder notiTitle = new SpannableStringBuilder(title_alert);
                notiTitle.setSpan (new CustomTypefaceSpan(ctx,regularBold), 0, notiTitle.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                contentViewBig.setTextViewText(R.id.noti_title, notiTitle);
                contentViewSmall.setTextViewText(R.id.noti_title, notiTitle);
                SpannableStringBuilder notiBody = new SpannableStringBuilder(description);
                notiBody.setSpan (new CustomTypefaceSpan(ctx,regularBook), 0, notiBody.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                contentViewBig.setTextViewText(R.id.noti_body, notiBody);
                int maxlength=45;
                String descriptionSmall=description;
                if(description.length()>maxlength){
                    descriptionSmall=description.substring(0,maxlength)+"..";
                }
                SpannableStringBuilder notiBodySmall = new SpannableStringBuilder(descriptionSmall);
                notiBodySmall.setSpan (new CustomTypefaceSpan(ctx,regularBook), 0, notiBodySmall.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                contentViewSmall.setTextViewText(R.id.noti_body, notiBodySmall);
                String close_alert=ctx.getString(R.string.close_alert);
                PreyLogger.d("close_alert:"+close_alert);
                SpannableStringBuilder notiButton = new SpannableStringBuilder(close_alert);
                notiButton.setSpan (new CustomTypefaceSpan(ctx,regularBold), 0, notiButton.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                contentViewBig.setTextViewText(R.id.noti_button, notiButton);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder notification = new Notification.Builder(ctx, CHANNEL_ID)
                            .setSmallIcon(R.drawable.icon2)
                            .setCustomContentView(contentViewSmall)
                            .setCustomBigContentView(contentViewBig)
                            .setDeleteIntent(btPendingIntent2)
                            .setAutoCancel(true);
                    notificationManager.notify(notificationId, notification.build());
                } else {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.icon2)
                            .setCustomContentView(contentViewSmall)
                            .setCustomBigContentView(contentViewBig)
                            .setDeleteIntent(btPendingIntent2)
                            .setAutoCancel(true);
                    notificationManager.notify(notificationId, builder.build());
                }
            }
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "alert", "started", reason));
            PreyConfig.getPreyConfig(ctx).setNextAlert(true);
        } catch (Exception e) {
            PreyLogger.e("failed alert: " + e.getMessage(), e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId, UtilJson.makeMapParam("start", "alert", "failed", e.getMessage()));
        }
    }

    public void fullscreen(final int notificationId){
        try {
            PreyConfig.getPreyConfig(ctx).setNoficationPopupId(notificationId);
            PreyLogger.d("started alert");
            String title = "title";
            Bundle bundle = new Bundle();
            bundle.putString("title_message", title);
            bundle.putString("alert_message", description);
            Intent popup = new Intent(ctx, PopUpAlertActivity.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            popup.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtras(bundle);
            popup.putExtra("description_message", description);
            popup.putExtra("notificationId", notificationId);
            ctx.startActivity(popup);
            if(PreyUtils.isChromebook(ctx)){
                new Thread() {
                    public void run() {
                        String reason = null;
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "alert", "started", reason));
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "alert", "stopped", reason));
                    }
                }.start();
            }
        } catch (Exception e) {
        }
    }

}
