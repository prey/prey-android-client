/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.activities.PasswordHtmlActivity;
import com.prey.activities.PasswordNativeActivity;
import com.prey.services.PreySecureHtmlService;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import java.util.Date;

public class PreyDisablePowerOptionsReceiver extends BroadcastReceiver {

    public PreyDisablePowerOptionsReceiver() {
    }

    public static String stringExtra="prey";



    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    public void onReceive(Context context, Intent intent) {
        boolean disablePowerOptions = PreyConfig.getPreyConfig(context).isDisablePowerOptions();
        PreyLogger.d("PreyDisablePowerOptionsReceiver disablePowerOptions:"+ disablePowerOptions );
        if (disablePowerOptions) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        PreyLogger.d("PreyDisablePowerOptionsReceiver disablePowerOptions key:"+ key +" value:"+ value );
                    }
                }
                boolean flag = ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
                boolean lock=PreyConfig.getPreyConfig(context).isLockSet();
                boolean pinActivated=PreyConfig.getPreyConfig(context).getPinActivated();
                try {
                   if (!pinActivated||!lock) {

                        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isScreenOn();
                        String reason=intent.getStringExtra("reason");
                        if (isScreenOn && reason != null) {

                            PreyLogger.d("PreyDisablePowerOptionsReceiver reason:"+reason+" flag:"+flag+" lock:"+flag+" putextra:"+intent.getStringExtra(stringExtra));


                            String extra=intent.getStringExtra(stringExtra);

                            long time=PreyConfig.getPreyConfig(context).getTimeSecureLock( );
                            long now=new Date().getTime();
                            PreyLogger.d("PreyDisablePowerOptionsReceiver time:"+time+" now:"+now +" "+(now<time));
                            if( now<time){
                                extra="";
                            }
                            if(extra==null) {
                                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                                intentClose.putExtra(stringExtra, stringExtra);
                                context.sendBroadcast(intentClose);

                                String pinNumber=PreyConfig.getPreyConfig(context).getPinNumber();

                                PreyLogger.d("PreyDisablePowerOptionsReceiver pinNumber1:"+pinNumber);
                                if("globalactions".equals(reason)&& pinNumber!=null&& !"".equals(pinNumber)){
                                    PreyLogger.d("PreyDisablePowerOptionsReceiver pinNumber2:"+pinNumber);
                                    if(!PreyConfig.getPreyConfig(context).isOpenSecureService()) {
                                        PreyLogger.d("PreyDisablePowerOptionsReceiver pinNumber3:"+pinNumber);
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                                            PreyLogger.d("PreyDisablePowerOptionsReceiver pinNumber4:"+pinNumber);
                                            PreyConfig.getPreyConfig(context).setPinActivated(true);
                                            boolean isOverOtherApps=PreyConfig.getPreyConfig(context).isOverOtherApps();
                                            if(isOverOtherApps&&PreyConfig.getPreyConfig(context).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(context)) {
                                                Intent intentLock = new Intent(context, PreySecureHtmlService.class);
                                                context.startService(intentLock);
                                            }else {
                                                Intent intent4 = null;
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    intent4 = new Intent(context, PasswordHtmlActivity.class);
                                                } else {
                                                    intent4 = new Intent(context, PasswordNativeActivity.class);
                                                }
                                                intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent4);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    PreyLogger.e("error:"+e.getMessage(),e);
                }
            }
        }
    }

}

