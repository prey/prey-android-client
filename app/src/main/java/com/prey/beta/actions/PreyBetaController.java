/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.beta.services.PreyBetaRunnerService;

public class PreyBetaController {

    public static void startPrey(Context ctx) {
        startPrey(ctx, null);
    }

    public static void startPrey(Context ctx, final String cmd) {
        PreyConfig config = PreyConfig.getPreyConfig(ctx);
        PreyLogger.d("startPrey:"+config.isThisDeviceAlreadyRegisteredWithPrey());
        if (config.isThisDeviceAlreadyRegisteredWithPrey()) {
            config.setRun(true);
            // Run the actions runner directly instead of going through
            // startService(PreyBetaRunnerService). On Android 12+ that startService
            // call throws BackgroundServiceStartNotAllowedException whenever this
            // entry point fires from background (FCM, boot receiver, app onCreate
            // while the app is not visible). The service was a no-op proxy
            // anyway: PreyBetaRunnerService.onStart just spawned the same runner
            // we now spawn here. Equivalent behavior, no foreground requirement.
            try {
                new com.prey.beta.actions.PreyBetaActionsRunnner(cmd).run(ctx);
            } catch (Exception e) {
                PreyLogger.e("error startPrey:" + e.getMessage(), e);
            }
        }
    }

    public static void stopPrey(Context ctx) {
        ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
    }

}

