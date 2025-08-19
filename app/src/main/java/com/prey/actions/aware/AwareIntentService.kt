/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.location.PreyLocation

/**
 * AwareIntentService is an IntentService that handles geofencing events.
 */
class AwareIntentService : IntentService(PreyConfig.TAG) {

    /**
     * Handles the intent sent to this service.
     *
     * @param intent The intent to handle.
     */
    override fun onHandleIntent(intent: Intent?) {
        PreyLogger.d("AWARE AwareIntentService")
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        // If the geofencing event is not null, process it.
        if (geofencingEvent != null) {
            PreyLogger.d("AWARE AwareIntentService event:${(if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "GEOFENCING_IN" else "GEOFENCING_OUT")}")
            processGeofencingEvent(geofencingEvent)
        }
        stopSelf()
    }

    /**
     * Processes a geofencing event.
     *
     * @param event The geofencing event to process.
     */
    private fun processGeofencingEvent(event: GeofencingEvent) {
        if (event.hasError()) {
            onError(event.errorCode)
        } else {
            notifyGeofenceTransition(
                applicationContext,
                event.geofenceTransition,
                event.triggeringGeofences,
                event.triggeringLocation
            )
        }
    }

    /**
     * Notifies the geofence transition.
     *
     * @param context The context of the application.
     * @param geofenceTransition The type of geofence transition.
     * @param triggeringGeofences The list of geofences that triggered the transition.
     * @param location The location of the device.
     */
    private fun notifyGeofenceTransition(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>?,
        location: Location?
    ) {
        var transition = ""
        for (geofence in triggeringGeofences!!) {
            transition = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                GEOFENCING_IN
            } else {
                GEOFENCING_OUT
            }
        }
        if (GEOFENCING_OUT == transition) {
            PreyLogger.d("AWARE notifyGeofenceTransition transition:$transition")
            try {
                AwareController.getInstance().sendAware(context, PreyLocation(location))
                AwareController.getInstance().registerGeofence(context)
            } catch (e: Exception) {
                PreyLogger.e("AWARE notifyGeofenceTransition error:${e.message}", e)
            }
        }
    }

    /**
     * Logs an error message for a geofencing error.
     *
     * @param i The error code.
     */
    private fun onError(i: Int) {
        PreyLogger.d("AWARE onError in ${(if (i == Geofence.GEOFENCE_TRANSITION_ENTER) "GEOFENCING_IN" else "GEOFENCING_OUT")}")
    }

    companion object {
        const val GEOFENCING_OUT: String = "geofencing_out"
        const val GEOFENCING_IN: String = "geofencing_in"
    }

}