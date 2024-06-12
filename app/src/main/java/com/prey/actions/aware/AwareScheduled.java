/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

public class AwareScheduled {

    private static AwareScheduled instance = null;
    private Context context = null;
    private AlarmManager alarmMgr = null;
    private PendingIntent pendingIntent = null;

    private AwareScheduled(Context context) {
        this.context = context;
    }

    public synchronized static AwareScheduled getInstance(Context context) {
        if (instance == null) {
            instance = new AwareScheduled(context);
        }
        return instance;
    }

    public void run() {
        try {
            int minute = 15;
            Intent intent = new Intent(context, AlarmAwareReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                PreyLogger.d("----------setRepeating");
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
            } else {
                PreyLogger.d("----------setInexactRepeating");
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
            }
            PreyLogger.d(String.format("----------start aware [%s] AwareScheduled", minute));
        } catch (Exception e) {
            PreyLogger.e(String.format("----------Error AwareScheduled :%s", e.getMessage()), e);
        }
    }

    public void reset() {
        if (alarmMgr != null) {
            try {
                alarmMgr.cancel(pendingIntent);
            } catch (Exception e) {
                PreyLogger.d(String.format("----------Error AwareScheduled :%s", e.getMessage()));
            }
        }
    }
}
