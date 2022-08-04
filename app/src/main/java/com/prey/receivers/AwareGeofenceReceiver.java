/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.prey.PreyLogger;
import com.prey.actions.aware.AwareController;
import com.prey.actions.location.PreyLocation;

public class AwareGeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent==null) {
                PreyLogger.d( "AWARE AwareGeofenceReceiver geofencingEvent null");
                return;
            }
            if (geofencingEvent.hasError()) {
                PreyLogger.d( "AWARE AwareGeofenceReceiver hasError:" +geofencingEvent.toString());
                return;
            }
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            PreyLogger.d( "AWARE AwareGeofenceReceiver onReceive :"+ (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT?"EXIT":"ENTER"));
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Location location=geofencingEvent.getTriggeringLocation();
                try {
                    AwareController.sendAware(context,new PreyLocation(location));
                    AwareController.getInstance().run(context);
                } catch (Exception e) {
                    PreyLogger.e("AWARE AwareGeofenceReceiver error:" + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
    }

}
