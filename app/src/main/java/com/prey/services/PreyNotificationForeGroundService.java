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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
    }

    private void stopForegroundService() {
        PreyLogger.d("Stop foreground service.");
        stopForeground(true);
        stopSelf();
    }

}