/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.wipe.WipeUtil;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.exceptions.PreyException;

public class WipeAction extends PreyAction {

    public static final String DATA_ID = "wipe";
    public final String ID = "wipe";

    @Override
    public String textToNotifyUserOnEachReport(Context ctx) {
        return "";
    }

    @Override
    public boolean shouldNotify() {
        return false;
    }

    @Override
    public void execute(ActionJob actionJob, Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);

        String formatSim = getConfig().get("format_sim");
        PreyLogger.d("format_sim:"+formatSim);
        try {
            if ("y".equals(formatSim)) {
                WipeUtil.deleteSD();
            }
        } catch (Exception e) {
        }

        try {
            if (preyConfig.isFroyoOrAbove()) {
                PreyLogger.d("Wiping the device!!");
                FroyoSupport.getInstance(ctx).wipe();
            }
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(), e);
        }

    }

    @Override
    public boolean isSyncAction() {
        return false;
    }

    @Override
    public int getPriority() {
        return WIPE_PRIORITY;
    }

}

