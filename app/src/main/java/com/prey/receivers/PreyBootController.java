/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.json.actions.Report;
import com.prey.preferences.RunBackgroundCheckBoxPreference;
import com.prey.services.PreyDisablePowerOptionsService;
import com.prey.services.PreyLockHtmlService;
import com.prey.services.PreyLockService;

public class PreyBootController extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PreyLogger.d("Boot finished. Starting Prey Boot Service");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            String interval = PreyConfig.getPreyConfig(context).getIntervalReport();
            if (interval != null && !"".equals(interval)) {
                Report.run(context, Integer.parseInt(interval));
            }
            final Context ctx = context;
            new Thread() {
                public void run() {
                    try {
                        boolean disablePowerOptions = PreyConfig.getPreyConfig(ctx).isDisablePowerOptions();
                        if (disablePowerOptions) {
                            ctx.startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
                        } else {
                            ctx.stopService(new Intent(ctx, PreyDisablePowerOptionsService.class));
                        }
                    } catch (Exception e) {
                    }
                    try {
                        boolean runBackground = PreyConfig.getPreyConfig(ctx).isRunBackground();
                        if (runBackground) {
                            RunBackgroundCheckBoxPreference.notifyReady(ctx);
                        } else {
                            RunBackgroundCheckBoxPreference.notifyCancel(ctx);
                        }
                    } catch (Exception e) {
                    }
                }
            }.start();
            new Thread() {
                public void run() {
                    try {
                        String unlockPass = PreyConfig.getPreyConfig(ctx).getUnlockPass();
                        PreyLogger.d("unlockPass:" + unlockPass);
                        if (unlockPass != null && !"".equals(unlockPass)) {
                            if (PreyConfig.getPreyConfig(ctx).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(ctx)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    ctx.startService(new Intent(ctx, PreyLockHtmlService.class));
                                }else{
                                    ctx.startService(new Intent(ctx, PreyLockService.class));
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }.start();
        } else
            PreyLogger.e("Received unexpected intent " + intent.toString(), null);
    }

}
