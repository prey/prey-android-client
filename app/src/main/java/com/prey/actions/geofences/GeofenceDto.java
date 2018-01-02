/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;

import java.util.UUID;

public class GeofenceDto implements Comparable {


    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public float radius;
    public int expires;

    public String toString() {
        String sb = "id:" + id +
                " name:" + name +
                " latitude:" + latitude +
                " longitude:" + longitude +
                " radius:" + radius +
                " expires:" + expires + "\n";
        return sb;
    }

    public Geofence geofence() {
        id = UUID.randomUUID().toString();
        return new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    @Override
    public int compareTo(@NonNull Object another) {
        GeofenceDto other = (GeofenceDto) another;
        return name.compareTo(other.name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

}