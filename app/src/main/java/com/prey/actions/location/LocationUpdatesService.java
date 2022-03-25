/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.prey.PreyLogger;

import java.text.DecimalFormat;

public class LocationUpdatesService  extends Service {

    private LocationRequest mLocationRequest;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private Location mLocation;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void startForegroundService(final Context ctx) {
        PreyLogger.d("LocationUpdatesService Start foreground service.");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLocation = locationResult.getLastLocation();
                PreyLogger.d( "LocationUpdatesService New location_: " + mLocation);
                PreyLocationManager.getInstance(ctx).setLastLocation(new PreyLocation(mLocation));
                stop();
            }
        };
        try {
            createLocationRequest();
        } catch (Exception e) {PreyLogger.e( "LocationUpdatesService error:" + e.getMessage(),e);}
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException unlikely) {
             PreyLogger.e( "LocationUpdatesService error " + unlikely.getMessage(),unlikely);
        }
        getLastLocation(ctx);
    }

    private void createLocationRequest() {
        PreyLogger.d( "LocationUpdatesService createLocationRequest: " );
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastLocation(final Context ctx) {
        PreyLogger.d( "LocationUpdatesService getLastLocation" );
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            PreyLogger.d( "LocationUpdatesService onComplete" );
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                PreyLogger.d("LocationUpdatesService mLocation lat :"+round(mLocation.getLatitude())+" lng:"+round(mLocation.getLongitude())+" acc:"+round(mLocation.getAccuracy()));
                                PreyLocationManager.getInstance(ctx).setLastLocation(new PreyLocation(mLocation));
                            } else {
                                PreyLogger.d("LocationUpdatesService Failed to get location.");
                            }
                        }
                    });
            stop();
        } catch (SecurityException unlikely) {
            PreyLogger.e( "LocationUpdatesService error:" + unlikely.getMessage(),unlikely);
        }
    }

    public static double round(double value){
        double finalValue = 0;
        DecimalFormat df = new DecimalFormat("0.000000");
        String format = df.format(value);
        try {
            finalValue = (Double) df.parse(format);
        } catch (Exception e1) {
            try {
                Long finalValue2 = (Long) df.parse(format);
                finalValue = finalValue2.doubleValue();
            } catch (Exception e) {
                PreyLogger.e("Error:" + e.getMessage(), e);
            }
        }
        return finalValue;
    }

    private void stop() {
        PreyLogger.d( "LocationUpdatesService stop" );
        try {
            if(mLocationCallback!=null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception e) {
            PreyLogger.e( "error." + e.getMessage(),e);
        }
        try {
            stopSelf();
        } catch (Exception e) {
            PreyLogger.e( "error." + e.getMessage(),e);
        }
    }

}