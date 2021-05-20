/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import com.prey.receivers.AlarmScheduledReceiver;

public class PreyScheduled {

    private static PreyScheduled instance = null;
    private Context context = null;
    private AlarmManager alarmMgr = null;
    private PendingIntent alarmIntent = null;
    private PreyScheduled(Context context) {
        this.context = context;
    }

    public synchronized static PreyScheduled getInstance(Context context) {
        if (instance == null) {
            instance = new PreyScheduled(context);
        }
        return instance;
    }

    public void run(int minute) {
        final Context ctx = context;
        if (minute > 0) {
            reset();
            Intent intent = new Intent(context, AlarmScheduledReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MINUTE,minute);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * minute, alarmIntent);
            } else {
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * minute, alarmIntent);
            }
            PreyLogger.d("SCHEDULE_____________start scheduled [" + minute + "] alarmIntent");
        }
    }

    public void reset() {
        if (alarmMgr != null) {
            PreyLogger.d("_________________shutdown scheduled alarmIntent");
            alarmMgr.cancel(alarmIntent);
        }
    }

}