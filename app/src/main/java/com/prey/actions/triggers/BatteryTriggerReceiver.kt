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

/**
 * A BroadcastReceiver that listens for battery-related events and triggers corresponding actions.
 */
class BatteryTriggerReceiver : TriggerReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
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