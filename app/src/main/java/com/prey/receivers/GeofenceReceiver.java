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
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.location.PreyLocation;

import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            GeofencingEvent event = GeofencingEvent.fromIntent(intent);
            if (event != null) {
                if (event.hasError()) {
                    PreyLogger.d("GEO GeofenceReceiver hasError:" +event.toString());
                } else {
                    notifyGeofenceTransition(context,  event.getGeofenceTransition(), event.getTriggeringGeofences(), event.getTriggeringLocation());
                }
            }
        } catch (Exception e) {
            PreyLogger.e("GEO GeofenceReceiver error:" + e.getMessage(), e);
        }
    }

    private void notifyGeofenceTransition(Context context, int geofenceTransition, List<Geofence> triggeringGeofences, Location location) {
    }

}