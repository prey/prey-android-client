/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.autoconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

public class AutoConnectAlarmReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PreyLogger.d("______________________________");
            PreyLogger.d("______________________________");
            PreyLogger.d("----------AutoConnect AlarmReceiver onReceive");
            final Context ctx = context;
            new Thread() {
                public void run() {
                    new AutoConnectService().run(ctx);
                }
            }.start();
        } catch (Exception e) {
            PreyLogger.e("AutoConnect AlarmReceiver error:" + e.getMessage(), e);
        }
    }

}