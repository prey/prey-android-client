/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.prey.kotlin.PreyLogger

class PreyNotificationForeGroundService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null) {
            startForegroundService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
    }

    private fun stopForegroundService() {
        PreyLogger.d("Stop foreground service.")
        stopForeground(true)
        stopSelf()
    }
}