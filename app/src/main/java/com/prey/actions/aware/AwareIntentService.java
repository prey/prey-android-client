/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.geofences.GeofenceController;
import com.prey.actions.geofences.GeofenceDataSource;
import com.prey.actions.location.PreyLocation;

import java.util.List;

public class AwareIntentService extends IntentService {

    public AwareIntentService() {
        super(PreyConfig.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PreyLogger.d("AWARE AwareIntentService");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            PreyLogger.d("AWARE AwareIntentService event:"+(event.getGeofenceTransition()== Geofence.GEOFENCE_TRANSITION_ENTER?"GEOFENCING_IN":"GEOFENCING_OUT"));
            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {
                notifyGeofenceTransition(getApplicationContext(),  event.getGeofenceTransition(), event.getTriggeringGeofences(), event.getTriggeringLocation());
            }
        }
        stopSelf();
    }

    private void notifyGeofenceTransition(Context context, int geofenceTransition, List<Geofence> triggeringGeofences, Location location) {
        GeofenceDataSource dataSource = new GeofenceDataSource(context);
        String transition = "";
        for (Geofence geofence : triggeringGeofences) {
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                transition = GeofenceController.GEOFENCING_IN;
            } else {
                transition = GeofenceController.GEOFENCING_OUT;
            }
        }
        if(GeofenceController.GEOFENCING_OUT.equals(transition)) {
            PreyLogger.d("AWARE notifyGeofenceTransition transition:"+transition);
            try {
                AwareController.sendAware(context,new PreyLocation(location));
                AwareController.getInstance().run(context);
            } catch (Exception e) {
                PreyLogger.e("AWARE notifyGeofenceTransition error:" + e.getMessage(), e);
            }
        }
    }

    private void onError(int i) {
        PreyLogger.d("AWARE onError in " + (i==Geofence.GEOFENCE_TRANSITION_ENTER?"GEOFENCING_IN":"GEOFENCING_OUT"));
    }

}
