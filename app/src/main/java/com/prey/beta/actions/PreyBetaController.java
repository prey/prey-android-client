/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.PermissionChecker;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.beta.services.PreyBetaRunnerService;

public class PreyBetaController {

    public static void startPrey(Context ctx) {
        startPrey(ctx, null);
    }

    public static void startPrey(Context ctx, final String cmd) {
        PreyConfig config = PreyConfig.getPreyConfig(ctx);
        PreyLogger.d("startPrey:"+config.isThisDeviceAlreadyRegisteredWithPrey());
        if (config.isThisDeviceAlreadyRegisteredWithPrey()) {
            // Cancelling the notification of the SMS that started Prey

            PreyConfig.getPreyConfig(ctx).setCanAccessCamara(PreyPermission.canAccessCamera(ctx));
            PreyConfig.getPreyConfig(ctx).setCanAccessCoarseLocation(PreyPermission.canAccessCoarseLocation(ctx));
            PreyConfig.getPreyConfig(ctx).setCanAccessFineLocation(PreyPermission.canAccessFineLocation(ctx));
            PreyConfig.getPreyConfig(ctx).setCanAccessReadPhoneState(PreyPermission.canAccessReadPhoneState(ctx));

            config.setRun(true);
            final Context context = ctx;

            new Thread(new Runnable() {

                public void run() {
                    //First need to stop a previous running instance.
                    context.stopService(new Intent(context, PreyBetaRunnerService.class));
                    Intent intentStart = new Intent(context, PreyBetaRunnerService.class);
                    if (cmd != null) {
                        intentStart.putExtra("cmd", cmd);
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

