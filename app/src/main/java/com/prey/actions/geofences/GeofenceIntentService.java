/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.PreyLocation;

import java.util.List;

public class GeofenceIntentService extends IntentService {

    public GeofenceIntentService() {
        super(PreyConfig.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            PreyLogger.d("GEO GeofenceIntentService");
            GeofencingEvent event = GeofencingEvent.fromIntent(intent);
            if (event != null) {
                if (event.hasError()) {
                    PreyLogger.d("GEO GeofenceIntentService hasError:" +event.toString());
                } else {
                    notifyGeofenceTransition(getApplicationContext(),  event.getGeofenceTransition(), event.getTriggeringGeofences(), event.getTriggeringLocation());
                }
            }
        } catch (Exception e) {
            PreyLogger.e("GEO GeofenceIntentService error:" + e.getMessage(), e);
        }
        stopSelf();
    }

    private void notifyGeofenceTransition(Context context, int geofenceTransition, List<Geofence> triggeringGeofences, Location location) {
        PreyLocation locationNow=null;
        try{
            locationNow=new PreyLocation(location);
        }catch (Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        GeofenceController.verifyGeozone(context,locationNow);
    }

}