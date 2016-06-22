/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
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
                event.getTriggeringLocation().getLongitude();
                event.getTriggeringLocation().getLatitude();
                event.getTriggeringLocation().getAccuracy();

                PreyLogger.d("***************transition:" + transition);

                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    List<String> geofenceIds = new ArrayList<>();
                    List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
                    notifyGeofenceTransition(getApplicationContext(), transition, triggeringGeofences, event.getTriggeringLocation());
                }
            }
        }
    }

    private void notifyGeofenceTransition(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences, Location location) {
        PreyLogger.d("notifyGeofenceTransition");

        for (Geofence geofence : triggeringGeofences) {
            String requestId=geofence.getRequestId();
            PreyLogger.d("geofence.getRequestId():" + requestId);
            try {
                Event event = new Event();
                String eventGeofenceTransition="";
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    eventGeofenceTransition="geofencing_in";
                } else {
                    eventGeofenceTransition="geofencing_out";
                }
                event.setName(eventGeofenceTransition);
                JSONObject jsonObjectStatus = new JSONObject();
                int geofenceMaximumAccuracy=PreyConfig.getPreyConfig(context).getGeofenceMaximumAccuracy();
                String newEventGeo=eventGeofenceTransition+"_"+requestId;
                String lastEventGeo=PreyConfig.getPreyConfig(context).getLastEventGeo();

                PreyLogger.d("newEventGeo:"+newEventGeo+" lastEventGeo:"+lastEventGeo);
                if(!newEventGeo.equals(lastEventGeo)) {
                    if (location.getAccuracy() > geofenceMaximumAccuracy) {
                        int i=0;
                        PreyLocation locationNow =null;
                        do {
                            locationNow = LocationUtil.dataPreyLocation(context,null);
                            i=i+1;
                        }while (i<3 &&locationNow.getAccuracy() > geofenceMaximumAccuracy);

                        PreyLogger.d("locationNow:" + locationNow.toString());
                        GeofenceDataSource dataSource = new GeofenceDataSource(context);
                        GeofenceDto geo = dataSource.getGeofences(geofence.getRequestId());
                        double distance = distance(geo, locationNow);
                        PreyLogger.d("geofenceMaximumAccuracy distance:" + distance + " geo.getRadius()" + geo.getRadius());
                        if (distance > geo.getRadius()) {
                            PreyLogger.d("geofenceMaximumAccuracy distance is greater ");
                        } else {
                            JSONObject info = new JSONObject();
                            info.put("id", Integer.parseInt(requestId));
                            info.put("lat", locationNow.getLat());
                            info.put("lng", locationNow.getLng());
                            info.put("accuracy", locationNow.getAccuracy());
                            info.put("method", locationNow.getMethod());
                            event.setInfo(info.toString());
                            new EventThread(this, event, jsonObjectStatus).start();
                            PreyConfig.getPreyConfig(context).setLastEventGeo(newEventGeo);
                        }
                    } else {
                        JSONObject info = new JSONObject();
                        info.put("id", Integer.parseInt(requestId));
                        info.put("lat", location.getLatitude());
                        info.put("lng", location.getLongitude());
                        info.put("accuracy", location.getAccuracy());
                        info.put("method", "native");
                        event.setInfo(info.toString());
                        new EventThread(this, event, jsonObjectStatus).start();
                        PreyConfig.getPreyConfig(context).setLastEventGeo(newEventGeo);
                    }
                }
            } catch (Exception e) {
                PreyLogger.e("notifyGeofenceTransition error:" + e.getMessage(), e);
            }
        }
    }

    private void onError(int i) {
        PreyLogger.d("***************Geofencing Error: " + i);
    }

    private double distance( GeofenceDto start,PreyLocation end){
        Location locStart = new Location("");
        locStart.setLatitude(start.getLatitude());
        locStart.setLongitude(start.getLongitude());

        Location locEnd = new Location("");
        locEnd.setLatitude(end.getLat());
        locEnd.setLongitude(end.getLng());

        return locStart.distanceTo(locEnd);
    }

}