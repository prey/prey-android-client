/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;


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

    private Location currentLocation = null;
    protected Boolean mRequestingLocationUpdates;
    private Context ctx;

    public void init(Context ctx) {
        this.ctx=ctx;
        PreyLogger.d("init");
        currentLocation=null;
        mRequestingLocationUpdates = false;
        buildGoogleApiClient();
    }

    public Location getLastLocation(Context ctx) {
        PreyLogger.d("getLastLocation is null:" + (currentLocation == null));
        return currentLocation;
    }

    public static final int GOOGLE_API_CLIENT_TIMEOUT_S = 10;
    public static final String GOOGLE_API_CLIENT_ERROR_MSG =
            "Failed to connect to GoogleApiClient (error code = %d)";

    protected synchronized void buildGoogleApiClient() {
        PreyLogger.i("Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(
                GOOGLE_API_CLIENT_TIMEOUT_S, TimeUnit.SECONDS);

        if (connectionResult.isSuccess() && mGoogleApiClient.isConnected()) {

            createLocationRequest();
        } else{
            PreyLogger.i(String.format(GOOGLE_API_CLIENT_ERROR_MSG,
                    connectionResult.getErrorCode()));
        }
    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(PreyConfig.UPDATE_INTERVAL);

        mLocationRequest.setFastestInterval(PreyConfig.FASTEST_INTERVAL);

        mLocationRequest.setPriority(PreyConfig.LOCATION_PRIORITY);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        PreyLogger.i("Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();

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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            currentLocation=mCurrentLocation;
        }
    }
}

