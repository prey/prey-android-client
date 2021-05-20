/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.location.Location;

public class PreyLocation {

    private double lat;
    private double lng;
    private float accuracy;
    private double altitude;
    private long timestamp;
    private String method="native";
    private Location location;

    public PreyLocation() {
    }

    public PreyLocation(Location loc) {
        if (loc != null) {
            this.lat = loc.getLatitude();
            this.lng = loc.getLongitude();
            this.accuracy = loc.getAccuracy();
            this.altitude = loc.getAltitude();
            this.timestamp = System.currentTimeMillis();
            this.location = loc;
        }
    }

    public PreyLocation(Location loc, String method) {
        this(loc);
        if (loc != null) {
            this.method = method;
        }
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long timestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return String.format("lat:%s - lng:%s - acc:%s - method:%s",lat,lng,accuracy,method);
    }

    public boolean isValid() {
        return (this.lat != 0 && this.lng != 0);
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Location getLocation() { return location; }

    public void setLocation(Location location) { this.location = location; }

}