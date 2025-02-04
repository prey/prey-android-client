/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.prey.PreyConfig
import com.prey.PreyLogger


class GeofenceIntentService : IntentService(PreyConfig.TAG) {
    override fun onHandleIntent(intent: Intent?) {
        try {
            PreyLogger.d("GEO GeofenceIntentService")
            val event = GeofencingEvent.fromIntent(intent!!)
            if (event != null) {
                if (event.hasError()) {
                    PreyLogger.d("GEO GeofenceIntentService hasError:$event")
                } else {
                    notifyGeofenceTransition(
                        applicationContext,
                        event.geofenceTransition,
                        event.triggeringGeofences,
                        event.triggeringLocation
                    )
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("GEO GeofenceIntentService error:" + e.message, e)
        }
        stopSelf()
    }

    private fun notifyGeofenceTransition(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>?,
        location: Location?
    ) {
    }
}