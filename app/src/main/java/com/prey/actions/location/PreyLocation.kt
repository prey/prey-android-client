/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.location.Location
import android.location.LocationManager

class PreyLocation {
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var accuracy: Float = 0f
    private var altitude: Double = 0.0
    private var timestamp: Long = 0
    private var method: String = "native"
    private var location: Location? = null

    constructor()

    constructor(loc: Location?) {
        if (loc != null) {
            this.lat = loc.latitude
            this.lng = loc.longitude
            this.accuracy = loc.accuracy
            this.altitude = loc.altitude
            this.timestamp = System.currentTimeMillis()
            this.location = loc
        }
    }

    constructor(loc: Location?, method: String) : this(loc) {
        if (loc != null) {
            this.method = method
        }
    }

    constructor(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        altitude: Float,
        time: Long,
        method: String
    ) {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = latitude
        location.longitude = longitude
        location.accuracy = accuracy
        location.altitude = altitude.toDouble()
        location.time = time
        this.lat = location.latitude
        this.lng = location.longitude
        this.accuracy = location.accuracy
        this.altitude = location.altitude
        this.timestamp = location.time
        this.location = location
        this.method = method
    }
    constructor(
        latitude: Int,
        longitude: Int,
        accuracy: Float,
        altitude: Float,
        time: Long,
        method: String
    ) {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude =  latitude.toDouble()
        location.longitude = longitude.toDouble()
        location.accuracy = accuracy
        location.altitude = altitude.toDouble()
        location.time = time
        this.lat = location.latitude
        this.lng = location.longitude
        this.accuracy = location.accuracy
        this.altitude = location.altitude
        this.timestamp = location.time
        this.location = location
        this.method = method
    }


    fun getMethod(): String {
        return method
    }

    fun setMethod(method: String?) {
        this.method = method!!
    }

    fun getLat(): Double {
        return lat
    }

    fun setLat(lat: Double) {
        this.lat = lat
    }

    fun getLng(): Double {
        return lng
    }

    fun setLng(lng: Double) {
        this.lng = lng
    }

    fun isValid(): Boolean {
        return (this.lat != 0.0 && this.lng != 0.0)
    }


    fun getAccuracy(): Float {
        return accuracy
    }

    fun setAccuracy(accuracy: Float) {
        this.accuracy = accuracy
    }

    fun setAccuracy(accuracy: Int) {
        this.accuracy = accuracy.toFloat()
    }

    fun getAltitude(): Double {
        return altitude
    }

    fun setAltitude(altitude: Double) {
        this.altitude = altitude
    }

    fun getTimestamp(): Long {
        return timestamp
    }

    fun setTimestamp(timestamp: Long) {
        this.timestamp = timestamp
    }

    fun getLocation(): Location? {
        return location
    }

    fun setLocation(location: Location?) {
        this.location = location
    }

    fun timestamp(): Long {
        return this.timestamp
    }

    override fun toString(): String {
        return String.format("lat:%s - lng:%s - acc:%s - method:%s", lat, lng, accuracy, method)
    }


}