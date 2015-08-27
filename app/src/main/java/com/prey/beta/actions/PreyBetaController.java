/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.beta.services.PreyBetaRunnerService;

public class PreyBetaController {

    public static void startPrey(Context ctx) {
        startPrey(ctx, null);
    }

    public static void startPrey(Context ctx, String cmd) {
        PreyConfig config = PreyConfig.getPreyConfig(ctx);
        if (config.isThisDeviceAlreadyRegisteredWithPrey()) {
            // Cancelling the notification of the SMS that started Prey
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancelAll();

            config.setRun(true);
            final Context context = ctx;
            final String command = cmd;
            new Thread(new Runnable() {

                public void run() {
                    //First need to stop a previous running instance.
                    context.stopService(new Intent(context, PreyBetaRunnerService.class));
                    Intent intentStart = new Intent(context, PreyBetaRunnerService.class);
                    if (command != null) {
                        intentStart.putExtra("cmd", command);
                    }
                    context.startService(intentStart);
                }
            }).start();

        }
    }

    public static void stopPrey(Context ctx) {
        ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
    }

}

