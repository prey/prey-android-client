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
import android.os.Build
import android.os.IBinder
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.activities.PermissionInformationActivity
import com.prey.PreyLogger
import com.prey.PreyPermission

/**
 * Service responsible for handling accessibility events.
 */
class PreyAccessibilityService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Called when the service is started. This method is called every time the service is started.
     *
     * @param intent The Intent that was used to start this service.
     * @param startId A unique integer representing this specific start request.
     */
    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val context: Context = this
        object : Thread() {
            override fun run() {
                var i = 0
                var run = true
                while (run) {
                    try {
                        sleep(1000)
                        val isAccessibility = PreyPermission.isAccessibilityServiceEnabled(context)
                        PreyLogger.d("PreyAccessibilityService [$i]$isAccessibility")
                        if (isAccessibility) {
                            run = false
                            var intentActivity: Intent? = null
                            intentActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                Intent(context, CheckPasswordHtmlActivity::class.java)
                            } else {
                                Intent(context, PermissionInformationActivity::class.java)
                            }
                            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intentActivity)
                            stopSelf()
                            break
                        }
                        //TODO:Waiting time for accessibility to be active
                        if (i > 40) {
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
        }.start()
    }
}