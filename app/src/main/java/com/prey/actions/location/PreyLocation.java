/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.location.Location;
import android.location.LocationManager;

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

    public PreyLocation(double latitude, double longitude,float accuracy,float altitude, long time, String method) {
        Location location=new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setAltitude(altitude);
        location.setTime(time);
        this.lat = location.getLatitude();
        this.lng = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.timestamp = location.getTime();
        this.location = location;
        this.method = method;
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