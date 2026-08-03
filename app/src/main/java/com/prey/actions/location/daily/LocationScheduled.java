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
import android.os.Build;

import com.prey.PreyLogger;

public class LocationScheduled {

    /** Minutes between daily-location checks. */
    private static final int INTERVAL_MINUTES = 15;

    private static final long INTERVAL_MILLIS = 1000L * 60 * INTERVAL_MINUTES;

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
     * Arms an immediate first daily-location check. Called at app start. The alarm re-arms
     * itself from {@link AlarmLocationReceiver} so it survives Doze without a repeating alarm.
     *
     * @param context
     */
    public void run(Context context) {
        scheduleAt(context, System.currentTimeMillis());
    }

    /**
     * Arms the next daily-location check {@code INTERVAL_MINUTES} from now. Called by the
     * receiver on each fire so exactly one alarm is ever pending.
     *
     * @param context
     */
    public void scheduleNext(Context context) {
        scheduleAt(context, System.currentTimeMillis() + INTERVAL_MILLIS);
    }

    /**
     * Schedules a single wake-up alarm at {@code triggerAtMillis}. Uses
     * {@code setAndAllowWhileIdle} on API 23+ so the alarm fires even in Doze (no exact-alarm
     * permission needed); on older APIs Doze does not exist so {@code setExact} is used.
     */
    private void scheduleAt(Context context, long triggerAtMillis) {
        try {
            Intent intent = new Intent(context, AlarmLocationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmMgr == null) {
                PreyLogger.d("DAILY----------LocationScheduled no AlarmManager");
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            PreyLogger.d(String.format("DAILY----------LocationScheduled scheduled at [%s]", triggerAtMillis));
        } catch (Exception e) {
            PreyLogger.e(String.format("DAILY----------Error LocationScheduled :%s", e.getMessage()), e);
        }
    }
}
