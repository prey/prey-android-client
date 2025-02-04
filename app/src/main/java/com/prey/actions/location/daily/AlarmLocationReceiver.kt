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

class AlarmLocationReceiver : BroadcastReceiver() {
    /**
     * Receiving method to send daily location
     *
     * @param context
     * @param intent
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            PreyLogger.d("DAILY______________________________")
            PreyLogger.d("DAILY----------AlarmLocationReceiver onReceive")
            val ctx = context
            object : Thread() {
                override fun run() {
                    DailyLocation().run(ctx)
                }
            }.start()
        } catch (e: Exception) {
            PreyLogger.e(String.format("DAILY AlarmLocationReceiver error:%s", e.message), e)
        }
    }
}