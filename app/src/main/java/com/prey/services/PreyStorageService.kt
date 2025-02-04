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
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.PreyLogger
import com.prey.PreyPermission

class PreyStorageService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Service that listens if storage permission is granted and changes view
     *
     * @param intent
     * @param startId
     */
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
                        val isStorage = PreyPermission.isExternalStorageManager(ctx)
                        PreyLogger.d(String.format("PreyStorageService: %b", isStorage))
                        if (isStorage) {
                            run = false
                            val intentActivity = Intent(ctx, CheckPasswordHtmlActivity::class.java)
                            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intentActivity)
                            stopSelf()
                        }
                        //TODO:Waiting time for storage to be active
                        if (run && i > 40) {
                            run = false
                            stopSelf()
                        }
                        i++
                    } catch (e: Exception) {
                        PreyLogger.e(String.format("Error: %s", e.message), e)
                    }
                }
            }
        }.start()
    }
}
