/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware.kotlin

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.prey.actions.geofences.kotlin.GeofenceController
import com.prey.actions.geofences.kotlin.GeofenceDataSource
import com.prey.actions.location.kotlin.PreyLocation
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger


class AwareIntentService : IntentService(PreyConfig.TAG) {

    override fun onHandleIntent(intent: Intent?) {
        PreyLogger.d("AWARE AwareIntentService")
        val event = GeofencingEvent.fromIntent(intent!!)
        if (event != null) {
            PreyLogger.d("AWARE AwareIntentService event:" + (if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "GEOFENCING_IN" else "GEOFENCING_OUT"))
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
        stopSelf()
    }

    private fun notifyGeofenceTransition(
        context: Context,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>?,
        location: Location?
    ) {
        val dataSource = GeofenceDataSource(context)
        var transition = ""
        for (geofence in triggeringGeofences!!) {
            transition = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                GeofenceController.GEOFENCING_IN
            } else {
                GeofenceController.GEOFENCING_OUT
            }
        }
        if (GeofenceController.GEOFENCING_OUT == transition) {
            PreyLogger.d("AWARE notifyGeofenceTransition transition:$transition")
            try {
                AwareController.getInstance().sendAware(context, PreyLocation(location))
                AwareController.getInstance().run(context)
            } catch (e: Exception) {
                PreyLogger.e("AWARE notifyGeofenceTransition error:" + e.message, e)
            }
        }
    }

    private fun onError(i: Int) {
        PreyLogger.d("AWARE onError in " + (if (i == Geofence.GEOFENCE_TRANSITION_ENTER) "GEOFENCING_IN" else "GEOFENCING_OUT"))
    }
}