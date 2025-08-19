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
import android.os.Build

import com.prey.PreyLogger
import com.prey.services.PreyDisablePowerOptionsService

/**
 * This class is a BroadcastReceiver that listens for a specific alarm and starts the PreyDisablePowerOptionsService
 * if the device's API level is less than P (Android 9.0).
 */
class AlarmDisablePowerReceiver : BroadcastReceiver() {

    /**
     * This method is called when the BroadcastReceiver receives an intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.d("______AlarmDisablePowerReceiver  onReceive_________")
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                context.startService(Intent(context, PreyDisablePowerOptionsService::class.java))
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

}