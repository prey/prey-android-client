/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import com.prey.PreyLogger;
import com.prey.activities.WelcomeActivity;

public class PreyOverlayService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent,startId);
            int i=0;
        boolean run=true;
        while(run){
            try{Thread.sleep(1000);
                if(canDrawOverlays()){
                    run=false;
                    Intent intentWelcome = new Intent(this, WelcomeActivity.class);
                    intentWelcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentWelcome);
                    stopSelf();
                    break;
                }
                if (i>120){
                    run=false;
                    stopSelf();
                    break;
                }
                i++;
            }catch (Exception e){
                PreyLogger.e("Errror e:"+e.getMessage(),e);
            }
        }
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(getApplicationContext());
    }
}
