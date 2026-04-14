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
import com.prey.util.ServiceStartCompat;

public class PreyBetaController {

    public static void startPrey(Context ctx) {
        startPrey(ctx, null);
    }

    public static void startPrey(Context ctx, final String cmd) {
        PreyConfig config = PreyConfig.getPreyConfig(ctx);
        PreyLogger.d("startPrey:"+config.isThisDeviceAlreadyRegisteredWithPrey());
        if (config.isThisDeviceAlreadyRegisteredWithPrey()) {
            config.setRun(true);
            final Context context = ctx;
            new Thread(new Runnable() {
                public void run() {
                    try{
                        context.stopService(new Intent(context, PreyBetaRunnerService.class));
                        Intent intentStart = new Intent(context, PreyBetaRunnerService.class);
                        if (cmd != null) {
                            intentStart.putExtra("cmd", cmd);
                        }
                        ServiceStartCompat.startServiceCompat(context, intentStart);
                    }catch (Exception e){
                        PreyLogger.e("error:"+e.getMessage(),e);
                    }
                }
            }).start();
        }
    }

    public static void stopPrey(Context ctx) {
        ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
    }

}
