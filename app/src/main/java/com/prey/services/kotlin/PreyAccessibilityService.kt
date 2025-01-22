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
import android.os.Build
import android.os.IBinder
import com.prey.activities.kotlin.CheckPasswordHtmlActivity
import com.prey.activities.kotlin.PermissionInformationActivity
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPermission

class PreyAccessibilityService : Service() {
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
                var i = 0
                var run = true
                while (run) {
                    try {
                        sleep(1000)
                        val isAccessibility = PreyPermission.isAccessibilityServiceEnabled(ctx)
                        PreyLogger.d("PreyAccessibilityService [$i]$isAccessibility")
                        if (isAccessibility) {
                            run = false
                            var intentActivity: Intent? = null
                            intentActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                Intent(ctx, CheckPasswordHtmlActivity::class.java)
                            } else {
                                Intent(ctx, PermissionInformationActivity::class.java)
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
                        PreyLogger.e("Error:" + e.message, e)
                    }
                }
            }
        }.start()
    }
}