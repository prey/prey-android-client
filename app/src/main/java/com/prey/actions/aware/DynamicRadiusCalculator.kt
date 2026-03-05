/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware

import android.location.Location

/**
 * A singleton object that calculates a dynamic radius in meters based on the speed of travel between two location points.
 *
 * This calculator is used to determine an appropriate geographical radius, which might be useful for features like geofencing or location-based alerts. The radius increases with the calculated speed, assuming that a faster-moving user covers more ground and requires a larger area of interest.
 */
object DynamicRadiusCalculator {

    /**
     * Calculates a radius in meters based on the speed of travel between two location points.
     *
     * This function determines the speed in kilometers per hour (km/h) by calculating the distance
     * and time elapsed between a previous and a current location update. Based on the calculated speed,
     * it returns a predefined radius. This is useful for creating dynamic geofences or areas of
     * interest that adapt to the user's movement speed (e.g., walking, driving).
     *
     * The time difference is coerced to be at least 60,000 milliseconds (1 minute) to prevent
     * excessively high speeds from being calculated over very short time intervals.
     *
     * The speed-to-radius mapping is as follows:
     * - Speed <= 6 km/h (walking/stationary): 300 meters
     * - Speed <= 30 km/h (slow-moving vehicle, cycling): 500 meters
     * - Speed <= 80 km/h (city/suburban driving): 1500 meters
     * - Speed > 80 km/h (highway driving): 3000 meters
     *
     * @param previous The previous location measurement.
     * @param previousTime The timestamp of the previous location measurement, in milliseconds.
     * @param current The current location measurement.
     * @param currentTime The timestamp of the current location measurement, in milliseconds.
     * @return The calculated radius in meters as a [Float].
     */
    fun calculateRadius(
        previous: Location,
        previousTime: Long,
        current: Location,
        currentTime: Long
    ): Float {
        val distanceMeters = previous.distanceTo(current)
        val timeMs = (currentTime - previousTime).coerceAtLeast(60_000)
        val hours = timeMs / 3_600_000.0
        val kmh = (distanceMeters / 1000.0) / hours
        return when {
            kmh <= 6 -> 300f
            kmh <= 30 -> 500f
            kmh <= 80 -> 1000f
            else -> 2000f
        }
    }

}