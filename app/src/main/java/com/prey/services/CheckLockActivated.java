/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.receivers.PreyDeviceAdmin;

public class CheckLockActivated extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;

        new Thread() {
            public void run() {
                boolean run = true;
                while (run) {
                    boolean isLockSet = PreyConfig.getPreyConfig(ctx).isLockSet();
                    if (!isLockSet) {
                        run = false;
                        stopSelf();
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                        if (!PreyDeviceAdmin.canDrawOverlays(ctx)) {
                            PreyDeviceAdmin.lockWhenYouNocantDrawOverlays(ctx);
                            stopSelf();
                            break;
                        }
                    } catch (Exception e) {
                        PreyLogger.e("CheckLockActivated Error:" + e.getMessage(), e);
                    }
                }
            }
        }.start();
    }

}
