/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2022 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.activities.CheckPasswordHtmlActivity;

public class PreyStorageService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    /**
     * Service that listens if storage permission is granted and changes view
     *
     * @param intent
     * @param startId
     */
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        new Thread() {
            public void run() {
                int i = 0;
                boolean run = true;
                while (run) {
                    try {
                        Thread.sleep(1000);
                        boolean isStorage = PreyPermission.isExternalStorageManager(ctx);
                        PreyLogger.d(String.format("PreyStorageService: %b", isStorage));
                        if (isStorage) {
                            run = false;
                            Intent intentActivity = new Intent(ctx, CheckPasswordHtmlActivity.class);
                            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intentActivity);
                            stopSelf();
                        }
                        //TODO:Waiting time for storage to be active
                        if (run && i > 40) {
                            run = false;
                            stopSelf();
                        }
                        i++;
                    } catch (Exception e) {
                        PreyLogger.e(String.format("Error: %s", e.getMessage()), e);
                    }
                }
            }
        }.start();
    }

}
