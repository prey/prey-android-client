/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prey.PreyLogger

/**
 * A BroadcastReceiver that receives daily location updates.
 */
class DailyLocationReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     * This method is responsible for handling the daily location update.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            PreyLogger.d("DAILY______________________________")
            PreyLogger.d("DAILY----------AlarmLocationReceiver onReceive")
            object : Thread() {
                override fun run() {
                    DailyLocation().run(context)
                }
            }.start()
        } catch (e: Exception) {
            PreyLogger.e("DAILY AlarmLocationReceiver error:${e.message}", e)
        }
    }
}