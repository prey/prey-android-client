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

public class BatteryTriggerReceiver extends TriggerReceiver {

    public void onReceive(final Context context, final Intent intent) {
        if (intent != null) {
            PreyLogger.d("Trigger BatteryTriggerReceiver:" + intent.getAction());
            String action = intent.getAction();
            String name = "";
            if (EventFactory.ACTION_POWER_DISCONNECTED.equals(action))
                name = "stopped_charging";
            if (EventFactory.ACTION_POWER_CONNECTED.equals(action))
                name = "started_charging";
            if (EventFactory.BATTERY_LOW.equals(action))
                name = "low_battery";
            if (!"".equals(name)) {
                execute(context, name);
            }
        }
    }
}
