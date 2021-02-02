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
import com.prey.services.PreySecureService;

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

    public static String stringExtra = "prey";

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    public void onReceive(Context context, Intent intent) {
        boolean disablePowerOptions = PreyConfig.getPreyConfig(context).isDisablePowerOptions();
        boolean canDrawOverlays = PreyPermission.canDrawOverlays(context);
        PreyLogger.d("PreyDisablePowerOptionsReceiver disablePowerOptions:" + disablePowerOptions + " canDrawOverlays:" + canDrawOverlays);
        if (canDrawOverlays && disablePowerOptions) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        PreyLogger.d("PreyDisablePowerOptionsReceiver disablePowerOptions key:" + key + " value:" + value);
                    }
                }
                boolean flag = ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
                try {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    boolean isScreenOn = pm.isScreenOn();
                    String reason = intent.getStringExtra("reason");
                    if (isScreenOn && reason != null) {
                        String extra = intent.getStringExtra(stringExtra);
                        PreyLogger.d("PreyDisablePowerOptionsReceiver reason:" + reason + " flag:" + flag + " lock:" + flag + " extra:" + extra);
                        long time = PreyConfig.getPreyConfig(context).getTimeSecureLock();
                        long now = new Date().getTime();
                        PreyLogger.d("PreyDisablePowerOptionsReceiver time:" + time + " now:" + now + " " + (now < time));
                        if (now < time) {
                            extra = "";
                        }
                        if (extra == null) {
                            Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                            intentClose.putExtra(stringExtra, stringExtra);
                            context.sendBroadcast(intentClose);
                            String pinNumber = PreyConfig.getPreyConfig(context).getPinNumber();
                            PreyLogger.d("PreyDisablePowerOptionsReceiver pinNumber:" + pinNumber);
                            if ("globalactions".equals(reason) && pinNumber != null && !"".equals(pinNumber)) {
                                PreyLogger.d("pinNumber:" + pinNumber);
                                PreyConfig.getPreyConfig(context).setPinActivated(pinNumber);
                                if (!PreyConfig.getPreyConfig(context).isOpenSecureService()) {
                                    PreyLogger.d("open PreySecureService");
                                    Intent intentLock = new Intent(context, PreySecureService.class);
                                    context.startService(intentLock);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    PreyLogger.e("error:" + e.getMessage(), e);
                }
            }
        }
    }

}
