/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences

import com.google.android.gms.location.Geofence
import com.prey.actions.location.PreyLocation
import java.util.UUID

class GeofenceDto {
    private var id: String? = null
    private var name: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var radius: Float = 0f
    private var expires: Int = 0
    private var type: String? = null

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("id:").append(id)
        sb.append(" name:").append(name)
        sb.append(" latitude:").append(latitude)
        sb.append(" longitude:").append(longitude)
        sb.append(" radius:").append(radius)
        sb.append(" expires:").append(expires).append("\n")
        sb.append(" type:").append(type).append("\n")
        return sb.toString()
    }

    fun geofence(): Geofence {
        id = UUID.randomUUID().toString()
        return Geofence.Builder()
            .setRequestId(id!!)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }


    val preyLocation: PreyLocation
        get() {
            val location = PreyLocation()
            location.setLat(latitude)
            location.setLng(longitude)
            return location
        }

    fun getType(): String {
        return type!!
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun getId(): String {
        return id!!
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun getName(): String {
        return name!!
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getLatitude(): Double {
        return latitude
    }

    fun setLatitude(latitude: Double) {
        this.latitude = latitude
    }

    fun getLongitude(): Double {
        return longitude
    }

    fun setLongitude(longitude: Double) {
        this.longitude = longitude
    }

    fun getRadius(): Float {
        return radius
    }

    fun setRadius(radius: Float) {
        this.radius = radius
    }

    fun getExpires(): Int {
        return expires
    }

    fun setExpires(expires: Int) {
        this.expires = expires
    }
}