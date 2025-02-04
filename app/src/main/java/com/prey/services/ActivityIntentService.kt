/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.IntentService
import android.content.Intent
import com.prey.PreyConfig
import com.prey.PreyLogger

class ActivityIntentService : IntentService(PreyConfig.TAG) {
    override fun onCreate() {
        super.onCreate()
        PreyLogger.d("ActivityIntentService onCreate")
    }

    override fun onHandleIntent(intent: Intent?) {
    }
}