/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.prey.events.factories.EventFactory
import com.prey.events.manager.EventManagerRunner
import com.prey.PreyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * EventReceiver is a BroadcastReceiver that listens for events and triggers the execution of the corresponding actions.
 */
class EventReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the device is registered with Prey
        val isDeviceRegistered =
            PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey()
        if (isDeviceRegistered) {
            val event = EventFactory.getEvent(context, intent)
            if (event != null) {
                // If an event is found, execute it in a new thread
                CoroutineScope(Dispatchers.IO).launch { EventManagerRunner(context, event) }
            }
        }
    }

}