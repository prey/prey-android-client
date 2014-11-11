package com.prey.actions.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.prey.PreyLogger;

public class PreyGooglePlayServiceLocation implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
 
	/*
	 * Constants for location update parameters
	 */
	// Milliseconds per second
	public static final int MILLISECONDS_PER_SECOND = 1000;
	// The update interval
	public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
	// A fast interval ceiling
	public static final int FAST_CEILING_IN_SECONDS = 1;
	// Update interval in milliseconds
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// A fast ceiling of update intervals, used when the app is visible
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private Location currentLocation = null;

	public void init(Context ctx) {
		PreyLogger.d("init");
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		/*
		 * Set the update interval
		 */
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mLocationClient = new LocationClient(ctx, this, this);
		mLocationClient.connect();
	}

	public Location getLastLocation(Context ctx) {
		try {
			if (currentLocation == null) {
				currentLocation = mLocationClient.getLastLocation();
			}
		} catch (Exception e) {
		}
		PreyLogger.d("getLastLocation is null:" + (currentLocation == null));
		return currentLocation;
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		PreyLogger.d("onConnectionFailed");
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				PreyLogger.d("CONNECTION_FAILURE_RESOLUTION_REQUEST");
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (Exception e) {
				PreyLogger.d("error:" + e.getMessage());
			}
		} else {
			PreyLogger.d("error:" + connectionResult.getErrorCode());
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		PreyLogger.d("onConnected");
		// TODO Auto-generated method stub
		startPeriodicUpdates();
	}

	@Override
	public void onDisconnected() {
		PreyLogger.d("onDisconnected");
		// TODO Auto-generated method stub
		stopPeriodicUpdates();
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	public void startPeriodicUpdates() {
		PreyLogger.d("startPeriodicUpdates");
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	public void stopPeriodicUpdates() {
		PreyLogger.d("stopPeriodicUpdates");
		mLocationClient.removeLocationUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		PreyLogger.d("onLocationChanged is null" + (location == null));
		// In the UI, set the latitude and longitude to the value received
		// mLatLng.setText(LocationUtils.getLatLng(this, location));
		this.currentLocation = location;
	}
}
