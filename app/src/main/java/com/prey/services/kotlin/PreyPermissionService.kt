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
import com.prey.activities.kotlin.OpenSettingsActivity

class PreyPermissionService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Service that calls the open activity OpenSettingsActivity
     *
     * @param intent
     * @param startId
     */
    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val ctx: Context = this
        val intentConfiguration = Intent(ctx, OpenSettingsActivity::class.java)
        intentConfiguration.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intentConfiguration)
    }
}