/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import com.prey.PreyConfig;
import com.prey.PreyLogger;


import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PreyGooglePlayServiceLocation implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected String mLastUpdateTime;

    protected Boolean mRequestingLocationUpdates;
    private Context ctx;

    public void init(Context ctx) {
        this.ctx = ctx;
        //PreyLogger.d("init");
        mCurrentLocation = null;
        mLastUpdateTime = null;
        mRequestingLocationUpdates = false;
        buildGoogleApiClient();
    }

    public Location getLastLocation(Context ctx) {
        return mCurrentLocation;
    }

    public static final int GOOGLE_API_CLIENT_TIMEOUT_S = 10;
    public static final String GOOGLE_API_CLIENT_ERROR_MSG =
            "Failed to connect to GoogleApiClient (error code = %d)";

    protected synchronized void buildGoogleApiClient() {
        //PreyLogger.d("Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(
                GOOGLE_API_CLIENT_TIMEOUT_S, TimeUnit.SECONDS);

        if (connectionResult.isSuccess() && mGoogleApiClient.isConnected()) {
            createLocationRequest();
            startLocationUpdates();
        } else {
            PreyLogger.d(String.format(GOOGLE_API_CLIENT_ERROR_MSG,
                    connectionResult.getErrorCode()));
        }
    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(PreyConfig.UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(PreyConfig.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(PreyConfig.LOCATION_PRIORITY_HIGHT);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //PreyLogger.d("Connected to GoogleApiClient");
    }

    @Override
    public void onLocationChanged(Location location) {
        //PreyLogger.d("onLocationChanged");
        mCurrentLocation = location;
        if (location != null) {
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            //PreyLogger.d("latitude:" + location.getLatitude() + " longitude:" + location.getLongitude() + " accuracy:" + location.getAccuracy());
            stopLocationUpdates();
        }


    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        PreyLogger.d("Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        PreyLogger.d("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    protected void startLocationUpdates() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            try {
                Looper.prepare();
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
                Looper.loop();
            }catch (Exception e){
                    PreyLogger.d("Error startLocationUpdates: "+e.getMessage());
            }
        }

    }


}

