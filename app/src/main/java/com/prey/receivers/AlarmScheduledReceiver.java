/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import com.prey.PreyLogger;
import com.prey.beta.actions.PreyBetaActionsRunnner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmScheduledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Same fix as PreyBetaController.startPrey: skip the startService hop
        // that throws BackgroundServiceStartNotAllowedException on Android 12+
        // and run the actions runner directly. BroadcastReceivers grant a
        // brief execution window that is enough for the runner to spin up
        // its own thread.
        try {
            new PreyBetaActionsRunnner(null).run(context);
        } catch (Exception e) {
            PreyLogger.e("Error PreyBetaRunnerService:" + e.getMessage(), e);
        }
    }

}