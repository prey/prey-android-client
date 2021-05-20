/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import com.prey.PreyLogger;
import com.prey.beta.services.PreyBetaRunnerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmScheduledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent intentRunner = new Intent(context, PreyBetaRunnerService.class);
            context.startService(intentRunner);
        }catch (Exception e){
            PreyLogger.e("Error PreyBetaRunnerService:"+e.getMessage(),e);
        }
    }

}