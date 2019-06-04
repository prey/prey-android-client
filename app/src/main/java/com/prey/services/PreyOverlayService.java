/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.R;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.PermissionInformationActivity;
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
        final Context ctx=this;

        new Thread() {
            public void run() {
                int i=0;
                boolean run=true;
            while(run){
                    try{Thread.sleep(1000);
                        PreyLogger.d("["+i+"] PreyOverlayService");
                        if(canDrawOverlays()){
                            run=false;
                            NotificationManager nManager = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));
                            nManager.cancelAll();
                            String message = getString(R.string.device_added_congratulations_text);
                            Bundle bundle = new Bundle();
                            bundle.putString("message", message);
                            Intent intentWelcome = new Intent(ctx, PermissionInformationActivity.class);
                            intentWelcome.putExtras(bundle);
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
                        PreyLogger.e("Error:"+e.getMessage(),e);
                    }
                }
            }
        }.start();
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(getApplicationContext());
    }
}
