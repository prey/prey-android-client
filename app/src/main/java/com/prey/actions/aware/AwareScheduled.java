/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
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
            String minuteSt = PreyConfig.getPreyConfig(context).getIntervalAware();
            PreyLogger.d("----------AwareScheduled start minute:" + minuteSt);
            if (PreyConfig.getPreyConfig(context).getAware() && minuteSt != null && !"".equals(minuteSt)) {
                int minute = Integer.parseInt(minuteSt);
                Intent intent = new Intent(context, AwareAlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                    PreyLogger.d("----------setRepeating Aware: " + minute);
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
                } else {
                    PreyLogger.d("----------setInexactRepeating Aware: " + minute);
                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
                }
                PreyLogger.d("----------start aware [" + minute + "] AwareScheduled");
            }
        } catch (Exception e) {
            PreyLogger.d("----------Error AwareScheduled :" + e.getMessage());
        }
    }

    public void reset() {
        if (alarmMgr != null) {
            alarmMgr.cancel(pendingIntent);
        }
    }

}
