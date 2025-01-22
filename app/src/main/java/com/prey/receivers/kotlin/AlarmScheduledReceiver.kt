/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prey.beta.services.kotlin.PreyBetaRunnerService
import com.prey.kotlin.PreyLogger

class AlarmScheduledReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val intentRunner = Intent(context, PreyBetaRunnerService::class.java)
            context.startService(intentRunner)
        } catch (e: Exception) {
            PreyLogger.e("Error PreyBetaRunnerService:" + e.message, e)
        }
    }
}