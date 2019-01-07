/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;
import com.prey.actions.report.ReportService;
import com.prey.services.PreyDisablePowerOptionsService;

public class AlarmDisablePowerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PreyLogger.d("______AlarmDisablePowerReceiver  onReceive_________");
        try {
            context.startService(new Intent(context, PreyDisablePowerOptionsService.class));
        }catch (Exception e){}
    }

}
