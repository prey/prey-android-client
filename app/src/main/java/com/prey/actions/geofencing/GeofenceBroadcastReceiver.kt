package com.prey.actions.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.d("AWARE GeofenceBroadcastReceiver onReceive")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            PreyLogger.d("AWARE GeofenceBroadcastReceiver geofencingEvent null");
            return;
        }
        if (geofencingEvent.hasError()) {
            val errorMessage =
                GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            PreyLogger.d("AWARE GeofenceBroadcastReceiver errorMessage: $errorMessage")
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        PreyLogger.d("AWARE GeofenceBroadcastReceiver onReceive :${(if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) "EXIT" else "ENTER")}")
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val location: Location? = geofencingEvent.triggeringLocation
            try {
                val preyLocation = PreyLocation(location)
                PreyLocationManager.getInstance().setLastLocation(preyLocation)
                PreyConfig.getInstance(context).setLocation(preyLocation)
                PreyConfig.getInstance(context).setLocationAware(preyLocation)
                AwareController.getInstance().sendAware(context, PreyLocation(location))
                GeofenceManager.getInstance(context).updateGeofence(location!!)
            } catch (e: Exception) {
                PreyLogger.e("AWARE AwareGeofenceReceiver error:${e.message}", e)
            }
        }
    }

}