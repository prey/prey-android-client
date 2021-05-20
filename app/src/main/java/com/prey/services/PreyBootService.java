/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.prey.PreyLogger;

public class PreyBootService extends Service {

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        PreyBootService getService() {
            return PreyBootService.this;
        }
    }

    @Override
    public void onCreate() {
        PreyLogger.d("Prey Boot Service Started!");
    }

    @Override
    public void onDestroy() {
        PreyLogger.d("Boot Service has been stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}