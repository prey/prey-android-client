/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.prey.PreyLogger
import com.prey.beta.actions.PreyBetaController

/**
 * AlarmScheduledReceiver is a BroadcastReceiver that listens for alarm events and starts the Prey service.
 */
class AlarmScheduledReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            PreyBetaController.getInstance().startPrey(context)
        } catch (e: Exception) {
            PreyLogger.e("Error AlarmScheduledReceiver:" + e.message, e)
        }
    }
}