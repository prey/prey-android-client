/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.location.PreyLocationManager;

/**
 * This service is intented to be running while Prey is active. While
 * running, it will be updating the last location available in PreyConfig
 * persitent storage
 */
public class LocationService extends Service {

    private Location lastRegisteredLocation;
    //private LocationManager networkLocationManager;
    private LocationManager androidLocationManager;

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        PreyLogger.d("LocationService is going to be started...");
        try {
            androidLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

                LocationProvider gpsLocationProvider = androidLocationManager.getProvider(LocationManager.GPS_PROVIDER);
                LocationProvider networkProvider = androidLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);
                if (gpsLocationProvider != null && androidLocationManager.isProviderEnabled(gpsLocationProvider.getName())) {
                    androidLocationManager.requestLocationUpdates(gpsLocationProvider.getName(), PreyConfig.UPDATE_INTERVAL, PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE,
                            gpsLocationListener);
                    PreyLogger.d("GPS Location provider has been started.");
                }
                if (networkProvider != null && androidLocationManager.isProviderEnabled(networkProvider.getName())) {
                    androidLocationManager.requestLocationUpdates(networkProvider.getName(), PreyConfig.UPDATE_INTERVAL / 4, PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE,
                            networkLocationListener);
                    PreyLogger.d("NETWORK Location provider has been started.");
                }
            } else {
                PreyLogger.d("___________ask for permission LocationService ACCESS_FINE_LOCATION");
            }
        }catch (Exception e){}
        PreyLogger.d("LocationService has been started...");
    }

    @Override
    public void onDestroy() {

        if (androidLocationManager != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    androidLocationManager.removeUpdates(gpsLocationListener);
                    androidLocationManager.removeUpdates(networkLocationListener);

            }

        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Location getLastRegisteredLocation() {
        return lastRegisteredLocation;
    }

    private void setNewLocation(Location newLocation) {
        PreyLogger.d("[" + newLocation.getProvider() + "] Fix found!. Accuracy: [" + newLocation.getAccuracy() + "]");

        if (lastRegisteredLocation == null) {
            //PreyLogger.d("-----> First fix. Set as last location!");
            lastRegisteredLocation = newLocation;
        } else {
            if (newLocation.getTime() - lastRegisteredLocation.getTime() > PreyConfig.LAST_LOCATION_MAX_AGE) {
                //Last registered fix was set more that 2 minutes ago. It's older so must be updated!
                //PreyLogger.d("-----> Old fix has expired (older than 2 minutes). Setting new fix as last location!");
                lastRegisteredLocation = newLocation;
            } else if (newLocation.hasAccuracy() && (newLocation.getAccuracy() < lastRegisteredLocation.getAccuracy())) {
                //New location is more accurate than the previous one. Win!
                //PreyLogger.d("-------> Newer and more accurate fix. Set as last location!");
                lastRegisteredLocation = newLocation;
            }
        }
        PreyLocationManager.getInstance(getApplicationContext()).setLastLocation(new PreyLocation(lastRegisteredLocation));
    }

    private LocationListener gpsLocationListener = new LocationListener() {

        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusAsString = "Available";
            if (status == LocationProvider.OUT_OF_SERVICE)
                statusAsString = "Out of service";
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                statusAsString = "Temporarily Unavailable";
            PreyLogger.d("[LocationService] GPS Location provider status has changed: [" + statusAsString + "].");

        }

        public void onProviderEnabled(String provider) {
            PreyLogger.d("[LocationService] GPS Location Provider has been enabled: " + provider);
            //androidLocationManager.removeUpdates(gpsLocationListener);
            //androidLocationManager.requestLocationUpdates(provider, PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_INTERVAL, PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE, gpsLocationListener);
        }

        public void onProviderDisabled(String provider) {
            PreyLogger.d("[LocationService] GPS Location Provider has been disabled: " + provider);
        }

        public void onLocationChanged(Location location) {
            setNewLocation(location);
        }
    };

    private LocationListener networkLocationListener = new LocationListener() {

        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusAsString = "Available";
            if (status == LocationProvider.OUT_OF_SERVICE)
                statusAsString = "Out of service";
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                statusAsString = "Temporarily Unavailable";
            PreyLogger.d("[LocationService] Network Location provider status has changed: [" + statusAsString + "].");
        }

        public void onProviderEnabled(String provider) {
            PreyLogger.d("[LocationService] Network Location Provider has been enabled: " + provider);
        }

        public void onProviderDisabled(String provider) {
            PreyLogger.d("[LocationService] Network Location Provider has been disabled: " + provider);
        }

        public void onLocationChanged(Location location) {
            setNewLocation(location);
        }
    };
}

