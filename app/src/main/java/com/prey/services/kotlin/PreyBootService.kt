/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.prey.kotlin.PreyLogger

class PreyBootService : Service() {
    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: PreyBootService
            get() = this@PreyBootService
    }

    override fun onCreate() {
        PreyLogger.d("Prey Boot Service Started!")
    }

    override fun onDestroy() {
        PreyLogger.d("Boot Service has been stopped")
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}