/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

public class LocationScheduled {

    private static LocationScheduled instance = null;

    private LocationScheduled() {
    }

    public synchronized static LocationScheduled getInstance() {
        if (instance == null) {
            instance = new LocationScheduled();
        }
        return instance;
    }

    /**
     * Method that prepares an alarm to send the daily location
     *
     * @param context
     */
    public void run(Context context) {
        try {
            int minute = 15;
            Intent intent = new Intent(context, AlarmLocationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                PreyLogger.d("DAILY----------LocationScheduled setRepeating");
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
            } else {
                PreyLogger.d("DAILY----------LocationScheduled setInexactRepeating");
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
            }
            PreyLogger.d(String.format("DAILY----------start [%s] LocationScheduled", minute));
        } catch (Exception e) {
            PreyLogger.e(String.format("DAILY----------Error LocationScheduled :%s", e.getMessage()), e);
        }
    }
}