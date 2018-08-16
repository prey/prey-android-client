/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import com.prey.PreyLogger;
import com.prey.R;

public class PreyNotificationForeGroundService extends Service {

    public PreyNotificationForeGroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            startForegroundService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundService() {
        PreyLogger.d("Start foreground service.");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        String channelId = "channelId";
        String channelName = "Prey";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, channelId);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.icon_cog);
        builder.setContentTitle(getString(R.string.disable_power_ready));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setPriority(Notification.PRIORITY_MIN);
            builder.setOngoing(false);
            Notification notification = builder.build();
            startForeground(1, notification);
        }
    }

    private void stopForegroundService() {
        PreyLogger.d("Stop foreground service.");
        stopForeground(true);
        stopSelf();
    }
}
