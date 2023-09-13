/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2018 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.aware;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.location.LocationUpdatesService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.location.PreyLocationManager;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.receivers.AwareGeofenceReceiver;

import org.json.JSONObject;

import java.net.HttpURLConnection;
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

    public void init(Context ctx) {
        try{
            boolean isLocationAware=PreyConfig.getPreyConfig(ctx).getAware();
            PreyLogger.d("AWARE AwareController init isLocationAware:"+isLocationAware);
            if (isLocationAware) {
                PreyLocationManager.getInstance(ctx).setLastLocation(null);
                PreyLocation locationNow=LocationUtil.getLocation(ctx,null,false);
                if (locationNow!=null&&locationNow.getLat()!=0&&locationNow.getLng()!=0){
                    PreyLocationManager.getInstance(ctx).setLastLocation(locationNow);
                    PreyLogger.d("AWARE locationNow[i]:"+locationNow.toString());
                }
                new LocationUpdatesService().startForegroundService(ctx);
                PreyLocation locationAware = null;
                int i=0;
                while (i < LocationUtil.MAXIMUM_OF_ATTEMPTS) {
                    PreyLogger.d("AWARE getPreyLocationApp[i]:"+i);
                    try {
                        Thread.sleep(LocationUtil.SLEEP_OF_ATTEMPTS[i]*1000);
                    } catch (InterruptedException e) {
                    }
                    locationAware = PreyLocationManager.getInstance(ctx).getLastLocation();
                    if (locationAware!=null) {
                        locationAware.setMethod("native");
                        PreyLogger.d("AWARE init:" + locationAware.toString());
                    }else{
                        PreyLogger.d("AWARE init nulo" +i);
                    }
                    if (locationAware!=null&&locationAware.getLat()!=0&&locationAware.getLng()!=0){
                        break;
                    }
                    i++;
                }
                PreyLocation locationNow2 = sendAware(ctx, locationAware);
                if (locationNow2 != null) {
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
            int notificationResponsiveness= FileConfigReader.getInstance(ctx).getGeofenceNotificationResponsiveness();
            int radiusAware= FileConfigReader.getInstance(ctx).getRadiusAware();
            //remove
            List<String> listRemove = new ArrayList<String>();
            listRemove.add(GEO_AWARE_NAME);
            LocationServices.getGeofencingClient(ctx).removeGeofences(listRemove);
            //new
            PreyLocation locationOld=PreyConfig.getPreyConfig(ctx).getLocationAware();
            if (locationOld != null) {
                final double lat = locationOld.getLat();
                final double lng = locationOld.getLng();
                PreyLogger.d("AWARE lat:" + LocationUpdatesService.round(lat) + " lng:" + LocationUpdatesService.round(lng));
                if(lat==0||lng==0){
                    PreyLogger.d("AWARE is zero");
                    return;
                }
                List<Geofence> mGeofenceList = new ArrayList<Geofence>();
                mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                        .setRequestId(GEO_AWARE_NAME)
                        .setCircularRegion(lat, lng, radiusAware)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay(loiteringDelay)
                        .setNotificationResponsiveness(notificationResponsiveness)
                        .build());
                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_ENTER);
                builder.addGeofences(mGeofenceList);
                GeofencingRequest geofencingRequest = builder.build();
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Added for android 12
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, AwareGeofenceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                    LocationServices.getGeofencingClient(ctx).addGeofences(geofencingRequest, pendingIntent)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    PreyLogger.d("AWARE saveGeofence lat:" + LocationUpdatesService.round(lat) + " lng:" + LocationUpdatesService.round(lng));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    PreyLogger.e("AWARE saveGeofence error: " + e.getMessage(), e);
                                }
                            });
                }
            }else{
                PreyLogger.d("AWARE locationOld is null");
            }
        } catch (Exception e) {
            PreyLogger.e("AWARE error:" + e.getMessage(), e);
        }
    }

    public static PreyLocation sendAware(Context ctx, PreyLocation locationAware) throws Exception{
        //get location
        PreyLocation oldLocation = PreyConfig.getPreyConfig(ctx).getLocationAware();
        int distanceAware = PreyConfig.getPreyConfig(ctx).getDistanceAware();
        boolean mustSendAware = mustSendAware(ctx, oldLocation, locationAware, distanceAware);
        //send aware
        if (mustSendAware) {
            sendNowAware(ctx, locationAware);
            return locationAware;
        } else {
            return null;
        }
    }

    /**
     * Method to if location must send
     * @param ctx Context
     * @param oldLocation Old location
     * @param newLocation New location
     * @param distanceAware Minimum difference between locations
     * @return returns if to send
     */
    public static boolean mustSendAware(Context ctx, PreyLocation oldLocation, PreyLocation newLocation, int distanceAware) {
        boolean sendAware = false;
        if (oldLocation == null) {
            if (newLocation != null) {
                sendAware = true;
                PreyConfig.getPreyConfig(ctx).setLocationAware(newLocation);
            }
        } else {
            if (newLocation != null) {
                double distance = LocationUtil.distance(oldLocation, newLocation);
                PreyLogger.d("AWARE distance:" + distance + " > " + distanceAware);
                if (distance > distanceAware) {
                    sendAware = true;
                    PreyConfig.getPreyConfig(ctx).setLocationAware(newLocation);
                }
            }
        }
        return sendAware;
    }

    public static void getSendNowAware(Context ctx) throws Exception{
        PreyLocation locationNow= LocationUtil.getLocation(ctx, null, false);
        PreyLogger.d("AWARE locationNow:"+locationNow.toString());
        sendNowAware(ctx,locationNow);
    }

    /**
     * Method that sends the location
     * @param ctx Context
     * @param locationNow  location
     * @return returns PreyHttpResponse
     */
    public static PreyHttpResponse sendNowAware(Context ctx, PreyLocation locationNow) throws Exception {
        PreyHttpResponse preyResponse = null;
        if (locationNow == null || locationNow.getLat() == 0 || locationNow.getLng() == 0) {
            PreyLogger.d("AWARE sendNowAware is zero");
            return preyResponse;
        }
        boolean isLocationAware = PreyConfig.getPreyConfig(ctx).getAware();
        PreyLogger.d("AWARE sendNowAware isLocationAware:"+isLocationAware);
        if (isLocationAware){
            String messageId = null;
            String reason = null;
            double accD = Math.round(locationNow.getAccuracy() * 100.0) / 100.0;
            JSONObject json = new JSONObject();
            String method=locationNow.getMethod();
            if(method==null)
                method="native";
            json.put("lat", locationNow.getLat());
            json.put("lng", locationNow.getLng());
            json.put("accuracy", accD);
            json.put("method", method);
            JSONObject location = new JSONObject();
            location.put("location", json);
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            preyResponse = PreyWebServices.getInstance().sendLocation(ctx, location);
            if (preyResponse != null) {
                PreyLogger.d("AWARE getStatusCode :"+preyResponse.getStatusCode());
                if (preyResponse.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
                    PreyConfig.getPreyConfig(ctx).setAware(false);
                }
                PreyConfig.getPreyConfig(ctx).setAwareDate(PreyConfig.FORMAT_SDF_AWARE.format(new Date()));
                PreyLogger.d("AWARE sendNowAware:" + locationNow.toString());
            }
        }
        return preyResponse;
    }

}