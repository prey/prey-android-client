/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.content.Context;
import android.location.LocationManager;

public class PreyLocationManager {

	private PreyLocation lastLocation;
	private static PreyLocationManager _instance = null;
	LocationManager androidLocationManager = null;
	private Context ctx;

	private PreyLocationManager(Context ctx) {
		this.ctx = ctx;
		androidLocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
	}

	public static PreyLocationManager getInstance(Context ctx) {
		if (_instance == null)
			_instance = new PreyLocationManager(ctx);
		return _instance;
	}

	public void setLastLocation(PreyLocation loc) {
		this.lastLocation = loc;
	}

	public PreyLocation getLastLocation() {
		return lastLocation == null ? new PreyLocation() : lastLocation;
	}
	
	public boolean isGpsLocationServiceActive() {
		boolean gps_enabled = false;
		try {
			gps_enabled = androidLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		return gps_enabled;
	}
	
	public boolean isNetworkLocationServiceActive() {
		boolean network_enabled = false;
		try {
			network_enabled = androidLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}
		return network_enabled;
	}

	public boolean locationServicesEnabled() {
		return (isGpsLocationServiceActive() || isNetworkLocationServiceActive());

	}

}
