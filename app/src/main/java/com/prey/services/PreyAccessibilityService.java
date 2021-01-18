/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

public class PreyAccessibilityService {
/*
        extends Service {

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
                        boolean isAccessibility= PreyPermission.isAccessibilityServiceEnabled(ctx);
                        PreyLogger.d("PreyAccessibilityService ["+i+"]"+isAccessibility);
                        if(isAccessibility){
                            run=false;
                            Intent intentWelcome = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                intentWelcome = new Intent(ctx, CheckPasswordHtmlActivity.class);
                            }else{
                                intentWelcome = new Intent(ctx, PermissionInformationActivity.class);
                            }
                            intentWelcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intentWelcome);
                            stopSelf();
                            break;
                        }
                        if (i>60){
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

*/

}