package com.prey.actions.location;

/**
 * Created by oso on 24-08-15.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.prey.PreyLogger;
import com.prey.services.UtilityService;

import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class PreyGooglePlayServiceLocation  {


    private Location currentLocation = null;

    public void init(Context ctx) {
        PreyLogger.d("init");


        UtilityService.requestLocation(ctx);
    }

    public Location getLastLocation(Context ctx) {
        /*try {
            if (currentLocation == null) {
                currentLocation = UtilityService.getLastLocation();
            }
        } catch (Exception e) {
        }
        */
        PreyLogger.d("getLastLocation is null:" + (currentLocation == null));
        return currentLocation;
    }


}

