/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.observer.ActionsController;
import com.prey.beta.actions.PreyBetaActionsRunnner;

public class PreyCommandRunnerService extends Service {

    private static final String CHANNEL_ID = "prey_runner";
    private static final int NOTIFICATION_ID = 2202;

    private final IBinder mBinder = new LocalBinder();
    public static boolean running = false;

    public class LocalBinder extends Binder {
        PreyCommandRunnerService getService() {
            return PreyCommandRunnerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startAsForegroundService();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        String cmd = null;
        try {
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("cmd")) {
                cmd = intent.getExtras().getString("cmd");
            }
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(), e);
        }
        PreyLogger.d("PreyCommandRunnerService has been started...:" + cmd);
        PreyBetaActionsRunnner exec = new PreyBetaActionsRunnner(cmd);
        running = true;
        exec.run(PreyCommandRunnerService.this);
    }

    @Override
    public void onDestroy() {
        ActionsController.getInstance(PreyCommandRunnerService.this).finishRunningJosb();
        running = false;
        try {
            stopForeground(true);
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(), e);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    private void startAsForegroundService() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Prey runner",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon2)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.pre_report_location))
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
}
