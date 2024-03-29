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
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.services.PreySecureService;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import org.json.JSONObject;

import java.util.Date;

public class PreyDisablePowerOptionsReceiver extends BroadcastReceiver {

    public PreyDisablePowerOptionsReceiver() {
    }

    public static String stringExtra = "prey";

    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    public void onReceive(Context context, Intent intent) {
        boolean disablePowerOptions = PreyConfig.getPreyConfig(context).isDisablePowerOptions();
        boolean canDrawOverlays = PreyPermission.canDrawOverlays(context);
        PreyLogger.d(String.format("PreyDisablePowerOptionsReceiver disablePowerOptions:%s canDrawOverlays: %s",disablePowerOptions,canDrawOverlays));
        if (canDrawOverlays && disablePowerOptions) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        PreyLogger.d(String.format("PreyDisablePowerOptionsReceiver disablePowerOptions key:%s value:%s",key,value));
                    }
                }
                boolean flag = ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
                try {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    boolean isScreenOn = pm.isScreenOn();
                    String reason = intent.getStringExtra("reason");
                    if (isScreenOn && reason != null) {
                        String extra = intent.getStringExtra(stringExtra);
                        PreyLogger.d(String.format("PreyDisablePowerOptionsReceiver reason:%s flag:%s extra:%s",reason,flag,extra));
                        long time = PreyConfig.getPreyConfig(context).getTimeSecureLock();
                        long now = new Date().getTime();
                        PreyLogger.d(String.format("PreyDisablePowerOptionsReceiver time:%s now:%s <%s",time,now,(now<time)));
                        if (now < time) {
                            extra = "";
                        }
                        if (extra == null) {
                            Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                            intentClose.putExtra(stringExtra, stringExtra);
                            context.sendBroadcast(intentClose);
                            String pinNumber = PreyConfig.getPreyConfig(context).getPinNumber();
                            boolean isOpenSecureService=PreyConfig.getPreyConfig(context).isOpenSecureService();
                            PreyLogger.d(String.format("PreyDisablePowerOptionsReceiver pinNumber:%s isOpenSecureService:%s",pinNumber,isOpenSecureService));
                            if ("globalactions".equals(reason) && pinNumber != null && !"".equals(pinNumber) && pinNumber.length()==4 ) {
                                PreyLogger.d("pinNumber:" + pinNumber);
                                PreyConfig.getPreyConfig(context).setPinActivated(pinNumber);
                                if (!isOpenSecureService) {
                                    PreyLogger.d("open PreySecureService");
                                    PreyConfig.getPreyConfig(context).setViewSecure(true);
                                    Intent intentLock = new Intent(context, PreySecureService.class);
                                    context.startService(intentLock);
                                    new Thread() {
                                        public void run() {
                                            try {
                                                JSONObject info = new JSONObject();
                                                info.put("PIN", pinNumber);
                                                Event event = new Event(Event.ANDROID_LOCK_PIN, info.toString());
                                                new Thread(new EventManagerRunner(context, event)).start();
                                            }catch (Exception e){
                                                PreyLogger.e("Error send Lock:"+e.getMessage(),e);
                                            }
                                        }
                                    }.start();
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