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

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.aware.AwareConfig;
import com.prey.json.actions.Report;
import com.prey.services.PreyDisablePowerOptionsService;
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
            final Context ctx=context;
            new Thread() {
                public void run() {
                    AwareConfig.getAwareConfig(ctx).init();
                }
            }.start();
            boolean disablePowerOptions = PreyConfig.getPreyConfig(context).isDisablePowerOptions();
            if (disablePowerOptions) {
                context.startService(new Intent(context, PreyDisablePowerOptionsService.class));
            } else {
                context.stopService(new Intent(context, PreyDisablePowerOptionsService.class));
            }
            if (PreyConfig.getPreyConfig(context).isLockSet()) {
                if(PreyConfig.getPreyConfig(context).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(context)) {
                    context.startService(new Intent(context, PreyLockService.class));
                }
            }
        } else
            PreyLogger.e("Received unexpected intent " + intent.toString(), null);
    }

}
