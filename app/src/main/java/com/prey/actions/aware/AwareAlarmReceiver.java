/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

public class AwareAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String minuteSt = PreyConfig.getPreyConfig(context).getIntervalAware();
            PreyLogger.d("______________________________");
            PreyLogger.d("______________________________");
            PreyLogger.d("----------AlarmAwareReceiver onReceive[" + minuteSt + "]");
            final Context ctx = context;
            new Thread() {
                public void run() {
                    new AwareService().run(ctx);
                }
            }.start();
        } catch (Exception e) {
            PreyLogger.e("_______AlarmAwareReceiver error:" + e.getMessage(), e);
        }
    }

}