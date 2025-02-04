/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context
import android.content.Intent
import com.prey.events.factories.EventFactory

class BatteryTriggerReceiver : TriggerReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val eventName = when (action) {
            EventFactory.ACTION_POWER_DISCONNECTED -> "stopped_charging"
            EventFactory.ACTION_POWER_CONNECTED -> "started_charging"
            EventFactory.BATTERY_LOW -> "low_battery"
            else -> return
        }
        execute(context, eventName)
    }
}