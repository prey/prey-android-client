/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;
import com.prey.events.factories.EventFactory;

public class SimTriggerReceiver extends TriggerReceiver {

    public static final String EXTRA_SIM_STATE = "ss";

    public void onReceive(Context context, final Intent intent) {
        if (intent != null) {
            PreyLogger.d("Trigger SimTriggerReceiver:" + intent.getAction());
            String action = intent.getAction();
            String state = intent.getExtras().getString(EXTRA_SIM_STATE);
            if (state == null) {
                return;
            }
            boolean run = false;
            if ("ABSENT".equals(state) && EventFactory.SIM_STATE_CHANGED.equals(action)) {
                String name = "hardware_changed";
                execute(context, name);
            }
        }
    }
}
