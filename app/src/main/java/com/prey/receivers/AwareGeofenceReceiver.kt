/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

import com.prey.PreyConfig
import com.prey.actions.aware.AwareController
import com.prey.actions.location.PreyLocation
import com.prey.PreyLogger

/**
 * A BroadcastReceiver that listens for geofencing events and triggers the corresponding actions.
 */
class AwareGeofenceReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.d("AWARE AwareGeofenceReceiver onReceive")
        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent == null) {
                PreyLogger.d("AWARE AwareGeofenceReceiver geofencingEvent null")
                return
            }
            if (geofencingEvent.hasError() && !PreyConfig.getInstance(context).isTest()) {
                PreyLogger.d("AWARE AwareGeofenceReceiver hasError:$geofencingEvent")
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition
            PreyLogger.d("AWARE AwareGeofenceReceiver onReceive :${(if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) "EXIT" else "ENTER")}")
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                val location = geofencingEvent.triggeringLocation
                try {
                    val preyLocation = PreyLocation(location)
                    PreyConfig.getInstance(context).setLocationAware(preyLocation)
                    PreyConfig.getInstance(context).setLocation(preyLocation);
                    AwareController.getInstance().sendAware(context, preyLocation)
                    AwareController.getInstance().initUpdateLocation(context)
                    AwareController.getInstance().registerGeofence(context)
                } catch (e: Exception) {
                    PreyLogger.e("Error: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

}