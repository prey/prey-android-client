/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
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

	public PreyLocation() {

	}

	public PreyLocation(Location loc) {
		if(loc!=null){
			this.lat = loc.getLatitude();
			this.lng = loc.getLongitude();
			this.accuracy = loc.getAccuracy();
			this.altitude = loc.getAltitude();
			this.timestamp = System.currentTimeMillis();
		}
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
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
		return "lat: " + lat + " - lng: " + lng;
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

}
