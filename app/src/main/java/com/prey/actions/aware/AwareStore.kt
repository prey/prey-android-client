/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.content.Context
import android.location.Location
import androidx.core.content.edit
import com.prey.actions.location.LocationUtil

/**
 * A persistent storage utility for managing the last known location and its capture timestamp.
 *
 * This object provides a simplified interface for saving and retrieving [Location] data
 * using Android's [android.content.SharedPreferences]. It is primarily used to maintain
 * location awareness across application sessions or service restarts.
 */
object AwareStore {

    private const val PREFS = "location_prefs"
    private const val TIME = "time"

    /**
     * Saves the provided [Location] and the current timestamp to SharedPreferences.
     *
     * This function persists the latitude, longitude, and the time of saving, which can be
     * retrieved later using the [load] function.
     *
     * @param context The [Context] used to access SharedPreferences.
     * @param location The [Location] object containing the latitude and longitude to be saved.
     */
    fun save(context: Context, location: Location) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                putFloat(LocationUtil.LAT, location.latitude.toFloat())
                    .putFloat(LocationUtil.LNG, location.longitude.toFloat())
                    .putLong(TIME, System.currentTimeMillis())
            }
    }

    data class Stored(
        val location: Location,
        val time: Long
    )

    /**
     * Loads the last saved location and its timestamp from SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @return A [Stored] object containing the location and time, or null if no location has been saved.
     */
    fun load(context: Context): Stored? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(LocationUtil.LAT)) return null
        val loc = Location("stored").apply {
            latitude = prefs.getFloat(LocationUtil.LAT, 0f).toDouble()
            longitude = prefs.getFloat(LocationUtil.LNG, 0f).toDouble()
        }
        return Stored(
            location = loc,
            time = prefs.getLong(TIME, 0L)
        )
    }

}