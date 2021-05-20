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

import com.prey.PreyConfig;
import com.prey.actions.ActionsRunnner;
import com.prey.actions.observer.ActionsController;

/**
 * This class wraps Prey execution as a services, allowing the OS to kill it and
 * starting it again in case of low resources. This way we ensure Prey will be
 * running until explicity stop it.
 */
public class PreyRunnerService extends Service {

    private final IBinder mBinder = new LocalBinder();
    public static boolean running = false;
    public static long startedAt = 0;
    public static long interval = 0;
    public static long pausedAt = 0;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PreyRunnerService getService() {
            return PreyRunnerService.this;
        }
    }

    @Override
    public void onCreate() {
        ActionsRunnner exec = new ActionsRunnner();
        running = true;
        startedAt = System.currentTimeMillis();
        exec.run(PreyRunnerService.this);
    }

    @Override
    public void onDestroy() {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(PreyRunnerService.this);
        ActionsController.getInstance(PreyRunnerService.this).finishRunningJosb();
        running = false;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

}