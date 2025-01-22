/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences.kotlin

import android.content.Context
import com.prey.kotlin.PreyLogger

class GeofenceDataSource(context: Context?) {
    private val dbHelper = GeofenceOpenHelper(context)

    fun createGeofence(geofence: GeofenceDto) {
        try {
            dbHelper.insertGeofence(geofence)
        } catch (e: Exception) {
            try {
                dbHelper.updateGeofence(geofence)
            } catch (e1: Exception) {
                PreyLogger.e("GEO error db update:" + e1.message, e1)
            }
        }
    }

    fun deleteGeofence(id: String) {
        dbHelper.deleteGeofence(id)
    }

    val allGeofences: List<GeofenceDto>
        get() = dbHelper.allGeofences

    fun getGeofences(id: String): GeofenceDto? {
        return dbHelper.getGeofence(id)
    }

    fun deleteAllGeofence() {
        dbHelper.deleteAllGeofence()
    }

    fun updateGeofenceType(id: String, type: String?) {
        dbHelper.updateGeofenceType(id, type)
    }

    fun updateGeofence(geo: GeofenceDto) {
        dbHelper.updateGeofence(geo)
    }
}