/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.receivers.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prey.events.factories.kotlin.EventFactory
import com.prey.events.manager.kotlin.EventManagerRunner
import com.prey.kotlin.PreyConfig

class EventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isDeviceRegistered =
            PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey()
        if (isDeviceRegistered) {
            val event = EventFactory.getEvent(context, intent)
            if (event != null) {
                Thread(EventManagerRunner(context, event)).start()
            }
        }
    }
}