/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.prey.json.actions.kotlin.Lock
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class CheckLockActivated : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val ctx: Context = this
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
                        if (!Lock.canDrawOverlays(ctx)) {
                            Lock.lockWhenYouNocantDrawOverlays(ctx)
                            stopSelf()
                            break
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("CheckLockActivated Error:" + e.message, e)
                    }
                }
            }
        }.start()
    }
}