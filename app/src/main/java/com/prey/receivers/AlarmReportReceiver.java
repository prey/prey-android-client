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

            final  Context ctx=context;
            new Thread() {
                public void run() {
                    new ReportService().run(ctx);
                }
            }.start();

        }catch(Exception e){
            PreyLogger.e("_______AlarmReportReceiver error:"+e.getMessage(),e);
        }


    }


}
