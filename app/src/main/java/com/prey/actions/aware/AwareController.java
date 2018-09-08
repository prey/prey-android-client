/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AwareController {

    public static String GEO_AWARE_NAME = "AWARE";
    private static AwareController INSTANCE;

    public static AwareController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AwareController();
        }
        return INSTANCE;
    }

    public void init(final Context ctx) {
        try{
            PreyLogger.d("AWARE AwareController init:"+AwareConfig.getAwareConfig(ctx).isLocationAware());
            if (AwareConfig.getAwareConfig(ctx).isLocationAware()) {
                PreyLocation locationAware = LocationUtil.getLocation(ctx, null, false);
                PreyLocation locationNow = sendAware(ctx, locationAware);
                if (locationNow != null) {
                    run(ctx);
                }
            }
        } catch (Exception e) {
            PreyLogger.e("AWARE error:" + e.getMessage(), e);
        }
    }

    public void run(final Context ctx) {
        PreyLogger.d("AWARE AwareController run");
        try {
            int loiteringDelay= FileConfigReader.getInstance(ctx).getGeofenceLoiteringDelay();
            int radiusAware= FileConfigReader.getInstance(ctx).getRadiusAware();
            //remove
            List<String> listRemove = new ArrayList<String>();
            listRemove.add(GEO_AWARE_NAME);
            LocationServices.getGeofencingClient(ctx).removeGeofences(listRemove);
            //new
            PreyLocation locationOld=PreyConfig.getPreyConfig(ctx).getLocationAware();
            double lat=locationOld.getLat();
            double lng=locationOld.getLng();
            PreyLogger.d("AWARE lat:" + lat + " lng:" + lng);
            List<Geofence> mGeofenceList = new ArrayList<Geofence>();
            mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                    .setRequestId(GEO_AWARE_NAME)
                    .setCircularRegion(lat, lng, radiusAware)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(loiteringDelay)
                    .setNotificationResponsiveness(0)
                    .build());
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_EXIT|GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(mGeofenceList);
            GeofencingRequest geofencingRequest = builder.build();
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(ctx, AwareIntentService.class);
                PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                LocationServices.getGeofencingClient(ctx).addGeofences(geofencingRequest,pendingIntent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    PreyLogger.d("AWARE saveGeofence");
                                }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    PreyLogger.e("AWARE saveGeofence error: " + e.getMessage(),e);
                                }
                        });
            }//if
        } catch (Exception e) {
            PreyLogger.e("AWARE error:" + e.getMessage(), e);
        }
    }


    public static synchronized PreyLocation sendAware(Context ctx, PreyLocation locationAware) throws Exception{
        //get location
        PreyLocation locationNow = locationAware;
        if (locationNow != null) {
            PreyLocation locationOld = PreyConfig.getPreyConfig(ctx).getLocationAware();
            if (locationOld != null) {
                //compare
                int maxDistance=PreyConfig.getPreyConfig(ctx).getDistanceAware();
                double distance = LocationUtil.distance(locationNow, locationOld);
                PreyLogger.d("AWARE distance:"+distance +" <= " + maxDistance);
                if (distance <= maxDistance){
                    locationNow=null;
                }
            }
            PreyConfig.getPreyConfig(ctx).setLocationAware(locationAware);
        }
        //send aware
        if (locationNow != null) {
            sendNowAware(ctx,locationNow);
        }
        return locationAware;
    }

    public static void getSendNowAware(Context ctx) throws Exception{
        PreyLocation locationNow= LocationUtil.getLocation(ctx, null, false);
        PreyLogger.d("AWARE locationNow:"+locationNow.toString());
        sendNowAware(ctx,locationNow);
    }

    private static void sendNowAware(Context ctx, PreyLocation locationNow) throws Exception{
        PreyLogger.d("AWARE sendNowAware");
        String messageId = null;
        String reason = null;
        double accD = Math.round(locationNow.getAccuracy() * 100.0) / 100.0;
        JSONObject json = new JSONObject();
        json.put("lat", Double.toString(locationNow.getLat()));
        json.put("lng", Double.toString(locationNow.getLng()));
        json.put("accuracy", Double.toString(accD));
        json.put("method", locationNow.getMethod());
        JSONObject location = new JSONObject();
        location.put("location", json);
        PreyHttpResponse preyResponse = PreyWebServices.getInstance().sendLocation(ctx, location);
        if (preyResponse != null) {
            if (preyResponse.getStatusCode() == 201) {
                PreyLogger.d("AWARE getStatusCode 201");
                AwareConfig.getAwareConfig(ctx).setLocationAware(false);
            }
            PreyConfig.getPreyConfig(ctx).setAwareDate(PreyConfig.FORMAT_SDF_AWARE.format(new Date()));
            PreyLogger.d("AWARE sendNowAware:"+locationNow.toString());
        }
    }



}
