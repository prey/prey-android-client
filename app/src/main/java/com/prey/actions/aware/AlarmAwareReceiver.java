/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;

public class AlarmAwareReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PreyLogger.d("______________________________");
            PreyLogger.d("______________________________");
            PreyLogger.d("----------AlarmAwareReceiver onReceive");
            final Context ctx = context;
            new Thread() {
                public void run() {
                    new AwareController().init(ctx);
                }
            }.start();
        } catch (Exception e) {
            PreyLogger.e(String.format("AlarmAwareReceiver error:%s", e.getMessage()), e);
        }
    }
}
