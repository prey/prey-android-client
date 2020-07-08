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
import android.os.Build;
import android.os.IBinder;

import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.LoginActivity;

public class PreyAccessibilityService extends Service {

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
                        PreyLogger.d("PreyAccessibilityService ["+i+"]");
                        if(isAccessibilityServiceEnabled()){
                            run=false;
                            Intent intentWelcome = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intentWelcome = new Intent(ctx, CheckPasswordHtmlActivity.class);
                            }else{
                                intentWelcome = new Intent(ctx, LoginActivity.class);
                            }
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

    private boolean isAccessibilityServiceEnabled() {
        return PreyPermission.isAccessibilityServiceEnabled(this);
    }

}
