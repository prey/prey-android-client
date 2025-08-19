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

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.lock.LockAction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

/**
 * Service responsible for checking if the device is locked and taking necessary actions.
 */
class CheckLockService : Service() {
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
        CoroutineScope(Dispatchers.IO).launch {
            checkLock(applicationContext)
        }
    }

    fun checkLock(context: Context) {
        var run = true
        while (run) {
            val unlockPass = PreyConfig.getInstance(context).getUnlockPass()
            if (unlockPass == null || "" == unlockPass) {
                run = false
                stopSelf()
                break
            }
            try {
                sleep(1000)
                if (!LockAction().canDrawOverlays(context)) {
                    LockAction().lockWhenYouNocantDrawOverlays(context)
                    stopSelf()
                    break
                }
            } catch (e: Exception) {
                PreyLogger.e("CheckLockActivated Error: ${e.message}", e)
            }
        }
    }

}