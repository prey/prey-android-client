package com.prey.services;

/**
 * Created by oso on 24-08-15.
 */

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.receivers.PreyDisablePowerOptionsReceiver;

public class PreyDisablePowerOptionsService extends Service {

    BroadcastReceiver mReceiver;

    public PreyDisablePowerOptionsService() {
        mReceiver = new PreyDisablePowerOptionsReceiver();

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            PreyLogger.e("Error, cause:"+e.getMessage(),e);
        }
        stopForeground(true);
    }

    public int onStartCommand(Intent intent, int i, int j) {
        boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
        if (disablePowerOptions) {
            IntentFilter intentfilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            registerReceiver(mReceiver, intentfilter);
        }
        return START_STICKY;
    }

}
