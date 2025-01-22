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
import android.os.Build
import com.prey.kotlin.PreyLogger
import com.prey.services.kotlin.PreyDisablePowerOptionsService

class AlarmDisablePowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.d("______AlarmDisablePowerReceiver  onReceive_________")
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                context.startService(Intent(context, PreyDisablePowerOptionsService::class.java))
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error: %s", e.message), e)
        }
    }
}
