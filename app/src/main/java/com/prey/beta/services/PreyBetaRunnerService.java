/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.prey.PreyLogger;
import com.prey.beta.actions.PreyBetaActionsRunnner;
import com.prey.actions.observer.ActionsController;

/**
 * This class wraps Prey execution as a services, allowing the OS to kill it and
 * starting it again in case of low resources. This way we ensure Prey will be
 * running until explicity stop it.
 *
 * @author Carlos Yaconi H.
 *
 */
public class PreyBetaRunnerService extends Service {

    private final IBinder mBinder = new LocalBinder();
    public static boolean running = false;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PreyBetaRunnerService getService() {
            return PreyBetaRunnerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        String cmd=null;
        try{
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("cmd")) {
                cmd = intent.getExtras().getString("cmd");
            }
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        PreyLogger.d("PreyRunnerService has been started...:"+cmd);
        PreyBetaActionsRunnner exec = new PreyBetaActionsRunnner(cmd);
        running = true;
        exec.run(PreyBetaRunnerService.this);
    }

    @Override
    public void onDestroy() {
        ActionsController.getInstance(PreyBetaRunnerService.this).finishRunningJosb();
        running = false;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

}