/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences.kotlin

import android.content.Context
import com.prey.actions.location.kotlin.PreyLocation

class GeofenceController {
    private val listBD: List<GeofenceDto>? = null
    private val listWeb: List<GeofenceDto>? = null
    private val mapBD: Map<String, GeofenceDto>? = null
    private val mapWeb: Map<String, GeofenceDto>? = null
    fun run(ctx: Context?) {
    }

    private fun updateZones(
        context: Context,
        listWeb: List<GeofenceDto>,
        listBD: List<GeofenceDto>,
        dataSource: GeofenceDataSource
    ) {
    }

    fun deleteAllZones(context: Context?) {
    }

    private fun convertMap(list: List<GeofenceDto>?): Map<String, GeofenceDto>? {
        if (list == null) {
            return null
        }
        val map: MutableMap<String, GeofenceDto> = HashMap()
        var i = 0
        while (list != null && i < list.size) {
            val geo = list[i]
            map[geo.getId()] = geo
            i++
        }
        return map
    }

    fun sendNotify(ctx: Context?, params: Map<String?, String?>?) {
    }

    fun initList(ctx: Context?, listBD: List<GeofenceDto?>?) {
    }

    fun verifyGeozone(ctx: Context?, locationNow: PreyLocation?) {
    }

    fun validateGeozone(
        context: Context?,
        geo: GeofenceDto?,
        maximumAccuracy: Int,
        locationNow: PreyLocation?,
        dataSource: GeofenceDataSource?
    ) {
    }

    companion object {
        const val GEOFENCING_OUT: String = "geofencing_out"
        const val GEOFENCING_IN: String = "geofencing_in"
        private var instance: GeofenceController? = null

        fun getInstance(): GeofenceController {
            if (instance == null) {
                instance = GeofenceController()
            }
            return instance!!
        }


    }
}