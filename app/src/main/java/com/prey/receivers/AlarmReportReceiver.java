/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.report.ReportService;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReportReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PreyLogger.d("______________________________");
            PreyLogger.d("______________________________");
            PreyLogger.d("----------AlarmReportReceiver onReceive");

            Intent intent2 = new Intent(context, ReportService.class);
            context.startService(intent2);
        }catch(Exception e){
            PreyLogger.d("_______AlarmReportReceiver error:"+e.getMessage());
        }


    }


}
