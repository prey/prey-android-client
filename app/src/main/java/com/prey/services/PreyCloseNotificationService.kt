/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.prey.events.factories.EventFactory

class PreyCloseNotificationService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Service that closes the notification
     *
     * @param intent
     * @param startId
     */
    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(EventFactory.NOTIFICATION_ID)
    }
}
