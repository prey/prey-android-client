/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

import com.prey.PreyLogger

/**
 * A BroadcastReceiver that listens for geofencing events and triggers the corresponding actions.
 */
class GeofenceReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val event = GeofencingEvent.fromIntent(intent)
            if (event != null) {
                if (event.hasError()) {
                    PreyLogger.d("GEO GeofenceReceiver hasError:$event")
                } else {
                    notifyGeofenceTransition(
                        context,
                        event.geofenceTransition,
                        event.triggeringGeofences,
                        event.triggeringLocation
                    )
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("GEO GeofenceReceiver error:" + e.message, e)
        }
    }

    private fun notifyGeofenceTransition(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>?,
        location: Location?
    ) {
    }
}