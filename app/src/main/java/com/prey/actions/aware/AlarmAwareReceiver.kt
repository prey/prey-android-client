/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prey.PreyLogger

/**
 * AlarmAwareReceiver is a BroadcastReceiver that listens for alarm events and initializes the AwareController.
 */
class AlarmAwareReceiver : BroadcastReceiver() {
    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            // Start a new thread to initialize the AwareController
            Thread {
                // Initialize the AwareController with the application context
                AwareController().initLastLocation(context.applicationContext)
                AwareController.getInstance().registerGeofence(context)
            }.start()
        } catch (e: Exception) {
            // Log any exceptions that occur during the onReceive method
            PreyLogger.e("AlarmAwareReceiver error: ${e.message}", e)
        }
    }
}