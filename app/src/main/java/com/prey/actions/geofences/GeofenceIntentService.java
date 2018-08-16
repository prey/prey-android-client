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

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                event.getTriggeringLocation().getLongitude();
                event.getTriggeringLocation().getLatitude();
                event.getTriggeringLocation().getAccuracy();



                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    List<String> geofenceIds = new ArrayList<>();
                    List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
                    notifyGeofenceTransition(getApplicationContext(), transition, triggeringGeofences, event.getTriggeringLocation());
                }
            }
        }
    }

    public static final String GEOFENCING_OUT="geofencing_out";
    public static final String GEOFENCING_IN="geofencing_in";

    private void notifyGeofenceTransition(
        Context context,
        int geofenceTransition,
        List<Geofence> triggeringGeofences, Location location) {
        PreyLogger.d("notifyGeofenceTransition  lat:"+location.getLatitude()+" lng:"+location.getLongitude()+" acc:"+location.getAccuracy());
        try {
            for (Geofence geofence : triggeringGeofences) {
                String requestId = geofence.getRequestId();
                PreyLogger.d("geofence.getRequestId():" + requestId);
                Event event = new Event();
                String transition = "";
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    transition = GEOFENCING_IN;
                } else {
                    transition = GEOFENCING_OUT;
                }
                event.setName(transition);
                String newEventGeo=transition+"_"+requestId;
                PreyLogger.d("event:"+transition);
                GeofenceDataSource dataSource = new GeofenceDataSource(context);
                GeofenceDto geo = dataSource.getGeofences(requestId);
                int geofenceMaximumAccuracy=PreyConfig.getPreyConfig(context).getGeofenceMaximumAccuracy();
                PreyLogger.d("geofenceMaximumAccuracy:"+geofenceMaximumAccuracy);
                if (!transition.equals(geo.getType())){
                    int i=0;
                    PreyLocation locationNow =null;
                    do {
                        locationNow = LocationUtil.getLocation(context,null,false);
                        if(locationNow!=null) {
                            PreyLogger.d("locationNow lat:" + locationNow.getLat() + " lng:" + locationNow.getLng() + " acc:" + locationNow.getAccuracy());
                        }
                        Thread.sleep(1000);
                        i=i+1;
                    }while (i<10 &&locationNow.getAccuracy() > geofenceMaximumAccuracy);
                    if(locationNow.getAccuracy() > geofenceMaximumAccuracy){
                        locationNow=null;
                    }
                    JSONObject jsonObjectStatus = new JSONObject();
                    if(locationNow!=null) {
                        double distance = distance(geo, locationNow);
                        PreyLogger.d("geofenceMaximumAccuracy distance:" + distance + " geo.getRadius()" + geo.getRadius()+" type:" + geo.getType());
                        if (GEOFENCING_IN.equals(transition)) {
                            if (distance >= geo.getRadius()) {
                                PreyLogger.d("geofenceMaximumAccuracy distance is greater ");
                            } else {
                                JSONObject info = new JSONObject();
                                info.put("id", Integer.parseInt(requestId));
                                info.put("lat", locationNow.getLat());
                                info.put("lng", locationNow.getLng());
                                info.put("accuracy", locationNow.getAccuracy());
                                info.put("method", locationNow.getMethod());
                                event.setInfo(info.toString());
                                dataSource.updateGeofenceType(geo.id,transition);
                                new EventThread(this, event, jsonObjectStatus, newEventGeo).start();
                            }
                        } else {
                            if (distance <= geo.getRadius()) {
                                PreyLogger.d("geofenceMaximumAccuracy distance is less ");
                            } else {
                                JSONObject info = new JSONObject();
                                info.put("id", Integer.parseInt(requestId));
                                info.put("lat", locationNow.getLat());
                                info.put("lng", locationNow.getLng());
                                info.put("accuracy", locationNow.getAccuracy());
                                info.put("method", locationNow.getMethod());
                                event.setInfo(info.toString());
                                dataSource.updateGeofenceType(geo.id,transition);
                                new EventThread(this, event, jsonObjectStatus, newEventGeo).start();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            PreyLogger.e("notifyGeofenceTransition error:" + e.getMessage(), e);
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
        return Math.round(locStart.distanceTo(locEnd));
    }

}