package com.prey.receivers


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

import com.prey.PreyLogger

/**
 * A BroadcastReceiver that listens for geofencing events.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) } ?: return
        if (geofencingEvent.hasError()) {
            val errorMessage =
                GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            PreyLogger.d(
                "AWARE onReceive: ${errorMessage}"
            )
            return
        }
        val alertString = "Geofence Alert :" +
                " Trigger ${geofencingEvent.triggeringGeofences}" +
                " Transition ${geofencingEvent.geofenceTransition}"
        PreyLogger.d(
            "AWARE ${alertString}"
        )
    }

}