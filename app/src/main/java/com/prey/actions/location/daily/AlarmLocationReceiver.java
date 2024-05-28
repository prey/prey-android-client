/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

public class AlarmLocationReceiver extends BroadcastReceiver {

    /**
     * Receiving method to send daily location
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PreyLogger.d("DAILY______________________________");
            PreyLogger.d("DAILY----------AlarmLocationReceiver onReceive");
            final Context ctx = context;
            new Thread() {
                public void run() {
                    new DailyLocation().run(ctx);
                }
            }.start();
        } catch (Exception e) {
            PreyLogger.e(String.format("DAILY AlarmLocationReceiver error:%s", e.getMessage()), e);
        }
    }

}
