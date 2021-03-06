/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import com.prey.PreyLogger;
import com.prey.actions.report.ReportService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent intentReport = new Intent(context, ReportService.class);
            context.startService(intentReport);
        }catch (Exception e){
            PreyLogger.e("Error ReportService:"+e.getMessage(),e);
        }
    }

}