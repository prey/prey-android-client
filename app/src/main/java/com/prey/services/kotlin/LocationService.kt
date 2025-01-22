/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.prey.actions.location.kotlin.PreyLocation
import com.prey.actions.location.kotlin.PreyLocationManager
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

/**
 * This service is intented to be running while Prey is active. While
 * running, it will be updating the last location available in PreyConfig
 * persitent storage
 */
class LocationService : Service() {
    var lastRegisteredLocation: Location? = null
        private set

    //private LocationManager networkLocationManager;
    private var androidLocationManager: LocationManager? = null

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private val mBinder: IBinder = LocalBinder()

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    override fun onCreate() {
        PreyLogger.d("LocationService is going to be started...")
        try {
            androidLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                val gpsLocationProvider =
                    androidLocationManager!!.getProvider(LocationManager.GPS_PROVIDER)
                val networkProvider =
                    androidLocationManager!!.getProvider(LocationManager.NETWORK_PROVIDER)
                val passiveProvider =
                    androidLocationManager!!.getProvider(LocationManager.PASSIVE_PROVIDER)
                val fusedProvider =
                    androidLocationManager!!.getProvider(LocationManager.FUSED_PROVIDER)
                if (gpsLocationProvider != null && androidLocationManager!!.isProviderEnabled(
                        gpsLocationProvider.name
                    )
                ) {
                    androidLocationManager!!.requestLocationUpdates(
                        gpsLocationProvider.name,
                        PreyConfig.UPDATE_INTERVAL,
                        PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE,
                        gpsLocationListener
                    )
                    PreyLogger.d("GPS Location provider has been started.")
                }
                if (networkProvider != null && androidLocationManager!!.isProviderEnabled(
                        networkProvider.name
                    )
                ) {
                    androidLocationManager!!.requestLocationUpdates(
                        networkProvider.name,
                        PreyConfig.UPDATE_INTERVAL / 4,
                        PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE,
                        networkLocationListener
                    )
                    PreyLogger.d("NETWORK Location provider has been started.")
                }
                if (passiveProvider != null && androidLocationManager!!.isProviderEnabled(
                        passiveProvider.name
                    )
                ) {
                    androidLocationManager!!.requestLocationUpdates(
                        passiveProvider.name,
                        PreyConfig.UPDATE_INTERVAL,
                        PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE,
                        passiveLocationListener
                    )
                    PreyLogger.d("Passive Location provider has been started.")
                }
                if (fusedProvider != null && androidLocationManager!!.isProviderEnabled(
                        fusedProvider.name
                    )
                ) {
                    androidLocationManager!!.requestLocationUpdates(
                        fusedProvider.name,
                        PreyConfig.UPDATE_INTERVAL,
                        PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE,
                        fusedLocationListener
                    )
                    PreyLogger.d("Fused Location provider has been started.")
                }
            } else {
                PreyLogger.d("___________ask for permission LocationService ACCESS_FINE_LOCATION")
            }
        } catch (e: Exception) {
            PreyLogger.e("Error LocationService.onCreate" + e.message, e)
        }
        PreyLogger.d("LocationService has been started...")
    }

    override fun onDestroy() {
        if (androidLocationManager != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                androidLocationManager!!.removeUpdates(gpsLocationListener)
                androidLocationManager!!.removeUpdates(networkLocationListener)
                androidLocationManager!!.removeUpdates(passiveLocationListener)
                androidLocationManager!!.removeUpdates(fusedLocationListener)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private fun setNewLocation(newLocation: Location) {
        PreyLogger.d("[" + newLocation.provider + "] Fix found!. Accuracy: [" + newLocation.accuracy + "]")
        if (lastRegisteredLocation == null) {
            //PreyLogger.d("-----> First fix. Set as last location!");
            lastRegisteredLocation = newLocation
        } else {
            if (newLocation.time - lastRegisteredLocation!!.time > PreyConfig.LAST_LOCATION_MAX_AGE) {
                //Last registered fix was set more that 2 minutes ago. It's older so must be updated!
                //PreyLogger.d("-----> Old fix has expired (older than 2 minutes). Setting new fix as last location!");
                lastRegisteredLocation = newLocation
            } else if (newLocation.hasAccuracy() && (newLocation.accuracy < lastRegisteredLocation!!.accuracy)) {
                //New location is more accurate than the previous one. Win!
                //PreyLogger.d("-------> Newer and more accurate fix. Set as last location!");
                lastRegisteredLocation = newLocation
            }
        }
        PreyLocationManager.getInstance().setLastLocation ( PreyLocation(lastRegisteredLocation))
    }

    private val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            var statusAsString = "Available"
            if (status == LocationProvider.OUT_OF_SERVICE) statusAsString = "Out of service"
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) statusAsString =
                "Temporarily Unavailable"
            PreyLogger.d("[LocationService] GPS Location provider status has changed: [$statusAsString].")
        }

        override fun onProviderEnabled(provider: String) {
            PreyLogger.d("[LocationService] GPS Location Provider has been enabled: $provider")
            //androidLocationManager.removeUpdates(gpsLocationListener);
            //androidLocationManager.requestLocationUpdates(provider, PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_INTERVAL, PreyConfig.LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE, gpsLocationListener);
        }

        override fun onProviderDisabled(provider: String) {
            PreyLogger.d("[LocationService] GPS Location Provider has been disabled: $provider")
        }

        override fun onLocationChanged(location: Location) {
            setNewLocation(location)
        }
    }

    private val networkLocationListener: LocationListener = object : LocationListener {
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            var statusAsString = "Available"
            if (status == LocationProvider.OUT_OF_SERVICE) statusAsString = "Out of service"
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) statusAsString =
                "Temporarily Unavailable"
            PreyLogger.d("[LocationService] Network Location provider status has changed: [$statusAsString].")
        }

        override fun onProviderEnabled(provider: String) {
            PreyLogger.d("[LocationService] Network Location Provider has been enabled: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            PreyLogger.d("[LocationService] Network Location Provider has been disabled: $provider")
        }

        override fun onLocationChanged(location: Location) {
            setNewLocation(location)
        }
    }

    private val passiveLocationListener: LocationListener = object : LocationListener {
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            var statusAsString = "Available"
            if (status == LocationProvider.OUT_OF_SERVICE) statusAsString = "Out of service"
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) statusAsString =
                "Temporarily Unavailable"
            PreyLogger.d(
                String.format(
                    "[LocationService] Network Location provider status has changed: [%s].",
                    statusAsString
                )
            )
        }

        override fun onProviderEnabled(provider: String) {
            PreyLogger.d(
                String.format(
                    "[LocationService] Passive Location Provider has been enabled: %s",
                    provider
                )
            )
        }

        override fun onProviderDisabled(provider: String) {
            PreyLogger.d(
                String.format(
                    "[LocationService] Passive Location Provider has been disabled: %s",
                    provider
                )
            )
        }

        override fun onLocationChanged(location: Location) {
            setNewLocation(location)
        }
    }


    private val fusedLocationListener: LocationListener = object : LocationListener {
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            var statusAsString = "Available"
            if (status == LocationProvider.OUT_OF_SERVICE) statusAsString = "Out of service"
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) statusAsString =
                "Temporarily Unavailable"
            PreyLogger.d(
                String.format(
                    "[LocationService] Network Location provider status has changed: [%s].",
                    statusAsString
                )
            )
        }

        override fun onProviderEnabled(provider: String) {
            PreyLogger.d(
                String.format(
                    "[LocationService] Fused Location Provider has been enabled: %s",
                    provider
                )
            )
        }

        override fun onProviderDisabled(provider: String) {
            PreyLogger.d(
                String.format(
                    "[LocationService] Fused Location Provider has been disabled: %s",
                    provider
                )
            )
        }

        override fun onLocationChanged(location: Location) {
            setNewLocation(location)
        }
    }
}

