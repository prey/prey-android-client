/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.geofences;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

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
import com.prey.events.Event;
import com.prey.events.manager.EventThread;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.receivers.GeofenceReceiver;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceController {

    private static GeofenceController INSTANCE;
    private List<GeofenceDto> listBD = null;
    private List<GeofenceDto> listWeb = null;
    private Map<String, GeofenceDto> mapBD = null;
    private Map<String, GeofenceDto> mapWeb = null;
    public static final String GEOFENCING_OUT="geofencing_out";
    public static final String GEOFENCING_IN="geofencing_in";

    public static GeofenceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeofenceController();
        }
        return INSTANCE;
    }

    public void run(Context ctx) {
    }

    private void updateZones( Context ctx,List<GeofenceDto> listWeb ,List<GeofenceDto> listBD,GeofenceDataSource dataSource){
    }

    public void deleteAllZones(Context ctx){
    }

    private Map<String, GeofenceDto> convertMap(List<GeofenceDto> list) {
        if(list==null){
            return null;
        }
        Map<String, GeofenceDto> map = new HashMap<String, GeofenceDto>();
        for (int i = 0; list != null && i < list.size(); i++) {
            GeofenceDto geo = list.get(i);
            map.put(geo.getId(), geo);
        }
        return map;
    }

    public void sendNotify(final Context ctx, final Map<String, String> params) {
    }

    public void initList(final Context ctx,List<GeofenceDto> listBD) {
    }

    public static void verifyGeozone(Context ctx,PreyLocation locationNow){
    }

    public static void validateGeozone(Context ctx,GeofenceDto geo,int maximumAccuracy,PreyLocation locationNow,GeofenceDataSource dataSource){
    }
}