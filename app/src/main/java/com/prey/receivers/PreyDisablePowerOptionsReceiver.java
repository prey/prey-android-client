/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

public class PreyDisablePowerOptionsReceiver extends BroadcastReceiver {

    public PreyDisablePowerOptionsReceiver() {
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    public void onReceive(Context context, Intent intent) {

        boolean disablePowerOptions = PreyConfig.getPreyConfig(context).isDisablePowerOptions();
        if (disablePowerOptions) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                boolean flag = ((KeyguardManager) context.getSystemService("keyguard")).inKeyguardRestrictedInputMode();
                boolean lock=PreyConfig.getPreyConfig(context).isLockSet();
                try {
                    if (flag||lock) {
                        PreyLogger.i("PreyDisablePowerOptionsReceiver flag:"+flag+" lock:"+flag+" javo:"+intent.getStringExtra("javo"));
                        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isScreenOn();
                        if (isScreenOn) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                            String javo=intent.getStringExtra("javo");
                            if(javo==null) {
                                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                                intentClose.putExtra("javo", "javo");
                                context.sendBroadcast(intentClose);
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

