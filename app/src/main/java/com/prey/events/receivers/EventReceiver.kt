/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prey.PreyConfig
import com.prey.events.factories.EventFactory
import com.prey.events.manager.EventManager

/**
 * [BroadcastReceiver] responsible for capturing and processing system or application-
 */
class EventReceiver : BroadcastReceiver() {

    /**
     * Receives and processes broadcast intents.
     *
     * This method performs safety checks on the incoming [context] and [intent], verifies
     * if the device is registered with Prey, and if so, identifies and processes the
     * specific event using [EventFactory] and [EventManager].
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent?) {
        val ctx = context ?: return
        val action = intent?.action ?: return
        val config = PreyConfig.getPreyConfig(ctx)
        if (!config.isThisDeviceAlreadyRegisteredWithPrey()) {
            return
        }
        EventFactory.getEvent(ctx, intent)?.let { event ->
            EventManager.processCoroutine(ctx, event)
        }
    }

}