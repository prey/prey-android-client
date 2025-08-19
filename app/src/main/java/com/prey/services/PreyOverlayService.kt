/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.R
import com.prey.activities.LoginActivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreyOverlayService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val context: Context = this
        CoroutineScope(Dispatchers.IO).launch {
            onStart(context)
        }
    }

    fun onStart(context: Context) {
        var i = 0
        var run = true
        while (run) {
            try {
                Thread.sleep(1000)
                PreyLogger.d("[$i] PreyOverlayService")
                if (canDrawOverlays()) {
                    run = false
                    val nManager =
                        (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    nManager.cancelAll()
                    val message = getString(R.string.device_added_congratulations_text)
                    val bundle = Bundle()
                    bundle.putString("message", message)
                    val intentWelcome =
                        Intent(context, LoginActivity::class.java)
                    intentWelcome.putExtras(bundle)
                    intentWelcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intentWelcome)
                    PreyConfig.getInstance(context).setLastEvent("on_start_overlay")
                    stopSelf()
                    break
                }
                if (i > 30) {
                    run = false
                    stopSelf()
                    break
                }
                i++
            } catch (e: Exception) {
                PreyLogger.e("Error: ${e.message}", e)
            }
        }
    }

    private fun canDrawOverlays(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return Settings.canDrawOverlays(applicationContext)
    }

}