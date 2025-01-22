/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.prey.actions.aware.kotlin.AwareController
import com.prey.actions.location.kotlin.PreyLocation
import com.prey.kotlin.PreyLogger

class AwareGeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent == null) {
                PreyLogger.d("AWARE AwareGeofenceReceiver geofencingEvent null")
                return
            }
            if (geofencingEvent.hasError()) {
                PreyLogger.d("AWARE AwareGeofenceReceiver hasError:$geofencingEvent")
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition
            PreyLogger.d("AWARE AwareGeofenceReceiver onReceive :" + (if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) "EXIT" else "ENTER"))
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                val location = geofencingEvent.triggeringLocation
                try {
                    AwareController.getInstance().sendAware(context, PreyLocation(location))
                    AwareController.getInstance().run(context)
                } catch (e: Exception) {
                    PreyLogger.e("AWARE AwareGeofenceReceiver error:" + e.message, e)
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }
}
