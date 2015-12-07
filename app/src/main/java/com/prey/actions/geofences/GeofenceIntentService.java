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

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationUtil;
import com.prey.events.Event;
import com.prey.events.manager.EventThread;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeofenceIntentService extends IntentService {

    public GeofenceIntentService() {
        super(PreyConfig.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PreyLogger.d("***************onHandleIntent");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                PreyLogger.d("***************transition:" + transition);
                HttpDataService location = LocationUtil.dataLocation(this);
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    List<String> geofenceIds = new ArrayList<>();
                    List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
                    notifyGeofenceTransition(getApplicationContext(), transition, triggeringGeofences, location);
                }
            }
        }
    }

    private void notifyGeofenceTransition(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences, HttpDataService location) {
        PreyLogger.d("notifyGeofenceTransition");
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            PreyLogger.d("geofence.getRequestId():" + geofence.getRequestId());
            triggeringGeofencesIdsList.add(geofence.getRequestId());
            try {
                Event event = new Event();
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                    event.setName("geofencing_in");
                else
                    event.setName("geofencing_out");
                JSONObject info = new JSONObject();
                info.put("id", Integer.parseInt(geofence.getRequestId()));
                info.put("lat", location.getDataList().get(LocationUtil.LAT));
                info.put("lng", location.getDataList().get(LocationUtil.LNG));
                info.put("accuracy", location.getDataList().get(LocationUtil.ACC));
                info.put("method", location.getDataList().get(LocationUtil.METHOD));
                event.setInfo(info.toString());
                JSONObject jsonObjectStatus = new JSONObject();
                new EventThread(this, event, jsonObjectStatus).start();
            } catch (Exception e) {
                PreyLogger.e("notifyGeofenceTransition error:" + e.getMessage(), e);
            }
        }
    }

    private void onError(int i) {
        PreyLogger.d("***************Geofencing Error: " + i);
    }

}
