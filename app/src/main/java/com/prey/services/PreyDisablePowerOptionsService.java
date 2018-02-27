/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.receivers.AlarmDisablePowerReceiver;
import com.prey.receivers.PreyDisablePowerOptionsReceiver;



public class PreyDisablePowerOptionsService extends Service {

    BroadcastReceiver mReceiver;

    public PreyDisablePowerOptionsService() {
        PreyLogger.i("PreyDisablePowerOptionsService  create ________");
        mReceiver = new PreyDisablePowerOptionsReceiver();

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        PreyLogger.i("PreyDisablePowerOptionsService  start ________");
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void onDestroy() {
        PreyLogger.i("PreyDisablePowerOptionsService  onDestroy__________");
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            PreyLogger.e("Error, cause:" + e.getMessage(), e);
        }
        /*
        boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
        if (disablePowerOptions){

            schedule();
        }*/
        stopForeground(true);
    }

    public int onStartCommand(Intent intent, int i, int j) {
        /*
        boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
        if (disablePowerOptions) {
            IntentFilter intentfilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            registerReceiver(mReceiver, intentfilter);
        }*/
        return START_STICKY;
    }

    public void onTaskRemoved(Intent rootIntent) {
        /*
        boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
        if (disablePowerOptions){
            schedule();
        }*/
    }

    private void schedule() {
        /*
        PreyLogger.i("PreyDisablePowerOptionsService  schedule_________");
        Intent intent = new Intent(getApplicationContext(), AlarmDisablePowerReceiver.class);
        PendingIntent alarmDisablePower = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10000L, alarmDisablePower);
        }else{
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10000L, alarmDisablePower);
        }*/
    }
}
