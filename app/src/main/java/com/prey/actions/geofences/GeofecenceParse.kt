/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences

import android.content.Context
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import org.json.JSONObject

object GeofecenceParse {
    fun getJSONFromUrl(context: Context): List<GeofenceDto>? {
        return try {
            val jsonString = PreyWebServices.getInstance().geofencing(context)
            parseJsonFromString(context, jsonString)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
            null
        }
    }

    fun parseJsonFromString(context: Context, jsonString: String?): List<GeofenceDto>? {
        val wrappedJsonString = "{\"prey\":$jsonString}"
        val listGeofence: MutableList<GeofenceDto> = ArrayList()
        try {
            val jsnobject = JSONObject(wrappedJsonString)
            val geofenceArray = jsnobject.getJSONArray("prey")
            for (i in 0 until geofenceArray.length()) {
                val geofenceJson = geofenceArray[i].toString()
                val geofenceObject = JSONObject(geofenceJson)
                val geofence = GeofenceDto()
                geofence.setId(geofenceObject.getString("id"))
                geofence.setName(geofenceObject.getString("name"))
                geofence.setLatitude(geofenceObject.getString("lat").toDouble())
                geofence.setLongitude(geofenceObject.getString("lng").toDouble())
                geofence.setRadius(geofenceObject.getString("radius").toFloat())
                listGeofence.add(geofence)
            }
        } catch (e: Exception) {
            return null
        }
        return listGeofence
    }
}