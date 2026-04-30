/*******************************************************************************
 * Created by Orlando Aliaga
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
import com.prey.actions.ActionsRunner;
import com.prey.actions.observer.ActionsController;

/**
 * This class wraps Prey execution as a services, allowing the OS to kill it and
 * starting it again in case of low resources. This way we ensure Prey will be
 * running until explicity stop it.
 *
 * @author Carlos Yaconi H.
 *
 */
public class PreyRunnerService extends Service {

    private final IBinder mBinder = new LocalBinder();
    public static boolean running = false;

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
        PreyLogger.d("has been started...:"+cmd);
        running = true;
        new Thread(new ActionsRunner(PreyRunnerService.this, cmd)).start();
    }

    @Override
    public void onDestroy() {
        ActionsController.getInstance(PreyRunnerService.this).finishRunningJosb();
        running = false;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

}
