/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

import com.prey.json.actions.Lock
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * Service responsible for checking if the device is locked and taking necessary actions.
 */
class CheckLockActivated : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Called when the service is started.
     *
     * @param intent The Intent that was used to start the service.
     * @param startId A unique integer representing the start request.
     */
    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val context: Context = this
        object : Thread() {
            override fun run() {
                var run = true
                while (run) {
                    val unlockPass = PreyConfig.getInstance(
                        applicationContext
                    ).getUnlockPass()
                    if (unlockPass == null || "" == unlockPass) {
                        run = false
                        stopSelf()
                        break
                    }
                    try {
                        sleep(1000)
                        if (!Lock().canDrawOverlays(context)) {
                            Lock().lockWhenYouNocantDrawOverlays(context)
                            stopSelf()
                            break
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("CheckLockActivated Error: ${e.message}", e)
                    }
                }
            }
        }.start()
    }
}