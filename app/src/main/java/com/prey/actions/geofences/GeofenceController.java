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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.prey.FileConfigReader;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.aware.AwareService;
import com.prey.actions.location.PreyLocation;
import com.prey.events.Event;
import com.prey.events.manager.EventThread;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

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
    private GeofenceDataSource dataSource = null;

    public static GeofenceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeofenceController();
        }
        return INSTANCE;
    }

    public void run(final Context ctx) {
        try {
            dataSource = new GeofenceDataSource(ctx);
            listBD = dataSource.getAllGeofences();
            listWeb = GeofecenceParse.getJSONFromUrl(ctx);
            updateZones(ctx,listWeb,listBD);
        } catch (Exception e) {
            PreyLogger.e("error run"+e.getMessage(),e);
        }
    }

    private void updateZones( Context ctx,List<GeofenceDto> listWeb ,List<GeofenceDto> listBD){
        List<GeofenceDto> listDelete=new ArrayList<>();
        List<GeofenceDto> listUpdate=new ArrayList<>();
        Map<String, GeofenceDto> mapBD = convertMap(listBD);
        Map<String, GeofenceDto> mapWeb = convertMap(listWeb);
        List<String> removeList = new ArrayList<String>();
        List<String> listRemove = new ArrayList<String>();
        List<GeofenceDto> listAdd = new ArrayList<GeofenceDto>();
        for(int i=0;listBD!=null&&i<listBD.size();i++){
            GeofenceDto dto=listBD.get(i);
            PreyLogger.d("bd dto.getId():"+dto.getId());
            if(!mapWeb.containsKey(dto.getId())){
                removeList.add(dto.getId());
                listRemove.add(dto.getId());
                dataSource.deleteGeofence(dto.getId());
            }
        }
        if(listRemove!=null&&listRemove.size()>0) {
            LocationServices.getGeofencingClient(ctx).removeGeofences(listRemove);
        }
        if (removeList != null && removeList.size() > 0) {
            String infoDelete = "[";
            for (int i = 0; removeList != null && i < removeList.size(); i++) {
                infoDelete += removeList.get(i);
                if (i + 1 < removeList.size()) {
                    infoDelete += ",";
                }
            }
            infoDelete += "]";
            PreyLogger.d("infoDelete:" + infoDelete);
            sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "stopped", infoDelete));
        }

        for(int i=0;listWeb!=null&&i<listWeb.size();i++) {
            GeofenceDto geo = listWeb.get(i);
            if(mapBD.containsKey(geo.getId())){
                dataSource.updateGeofence(geo);
            }else{
                dataSource.createGeofence(geo);
                listAdd.add(geo);
            }
        }
        String infoAdd = "[";
        for(int i=0;listAdd!=null&&i<listAdd.size();i++) {
            GeofenceDto geo = listAdd.get(i);
            infoAdd += geo.id;
            if (i + 1 < listAdd.size()) {
                infoAdd += ",";
            }
        }
        infoAdd += "]";
        if(listAdd!=null&&listAdd.size()>0) {
            sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "started", infoAdd));
        }
        if(listWeb!=null&&listWeb.size()>0) {
            initList(ctx, listWeb);
        }
    }

    public void deleteAllZones(Context ctx){
        listBD = dataSource.getAllGeofences();
        for(int i=0;listBD!=null&&i<listBD.size();i++) {
            GeofenceDto dto = listBD.get(i);
            dataSource.deleteGeofence(dto.getId());
        }
    }

    private Map<String, GeofenceDto> convertMap(List<GeofenceDto> list) {
        Map<String, GeofenceDto> map = new HashMap<String, GeofenceDto>();
        for (int i = 0; list != null && i < list.size(); i++) {
            GeofenceDto geo = list.get(i);
            map.put(geo.getId(), geo);
        }
        return map;
    }

    public void sendNotify(final Context ctx, final Map<String, String> params) {
        new Thread() {
            public void run() {
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, params);
            }
        }.start();
    }

    public void init(final Context ctx) {
        PreyLogger.d("_GeofenceController__init");
        try {
            GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
            List<GeofenceDto> listBD = dataSource.getAllGeofences();
            initList(ctx,listBD);
        } catch (Exception e) {
        }
    }

    public void initList(final Context ctx,List<GeofenceDto> listBD) {
            List<com.google.android.gms.location.Geofence> mGeofenceList = new ArrayList<Geofence>();
            final List<GeofenceDto> listToBdAdd = new ArrayList<GeofenceDto>();
            String info = "[";
            for (int i = 0; listBD != null && i < listBD.size(); i++) {
                GeofenceDto geo = listBD.get(i);
                listToBdAdd.add(geo);
                PreyLogger.d("__[START]___________id:" + geo.name + " lat:" + geo.latitude + " long:" + geo.longitude + " ra:" + geo.radius + " expires:" + geo.expires);
                int transitionTypes = com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
                mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                        .setRequestId(geo.id)
                        .setCircularRegion(geo.latitude, geo.longitude, geo.radius)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(transitionTypes)
                        .setLoiteringDelay(FileConfigReader.getInstance(ctx).getGeofenceLoiteringDelay())
                        .build());
                info += geo.id;
                if (i + 1 < listBD.size()) {
                    info += ",";
                }
            }
            info += "]";
            final String extraInfo = info;
            PreyLogger.d("info:" + extraInfo);
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(mGeofenceList);
            GeofencingRequest geofencingRequest = builder.build();
            try {
                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(ctx, GeofenceIntentService.class);
                        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        LocationServices.getGeofencingClient(ctx).addGeofences(
                                geofencingRequest,
                                pendingIntent
                        )
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        PreyLogger.d("********saveGeofence");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        PreyLogger.d("*********************Registering geofence failed: " + e.getMessage());
                                        sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "status:" + e.getMessage()));
                                    }
                                });
                    }
            } catch (Exception e) {
                    PreyLogger.e("error ---->isConnected:" + e.getMessage(), e);
                    sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "error:" + e.getMessage()));
            }
    }

    public static void verifyGeozone(Context ctx,PreyLocation locationNow){
        PreyLogger.d("________________Connection verifyGeozone");
        GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
        List<GeofenceDto> listBD = dataSource.getAllGeofences();
        for (int i = 0; listBD != null && i < listBD.size(); i++) {
            GeofenceDto geo = listBD.get(i);
            PreyLogger.d("________________geo id:"+geo.id+" type_:"+geo.getType()+" radi:"+geo.getRadius());
            int geofenceMaximumAccuracy=PreyConfig.getPreyConfig(ctx).getGeofenceMaximumAccuracy();
            PreyLogger.d("locationNow.getAccuracy:"+locationNow.getAccuracy() +" geofenceMaximumAccuracy:"+geofenceMaximumAccuracy);
            if(locationNow.getAccuracy() < geofenceMaximumAccuracy) {
                PreyLocation locationGeo=geo.getPreyLocation();
                double distance=AwareService.distance(locationNow,locationGeo);
                String transition="";
                PreyLogger.d("________________geo id:"+geo.id+" distance:"+distance);
                if (distance > geo.getRadius()) {
                    transition = GeofenceIntentService.GEOFENCING_OUT;
                } else {
                    transition = GeofenceIntentService.GEOFENCING_IN;
                }
                PreyLogger.d("________________geo id:"+geo.id+" newEventGeo:" + transition+" type:"+geo.getType());
                if (!transition.equals(geo.getType())) {
                    if(GeofenceIntentService.GEOFENCING_IN.equals(transition)||(GeofenceIntentService.GEOFENCING_OUT.equals(transition) && geo.getType()!=null)) {
                        try {
                            JSONObject info = new JSONObject();
                            info.put("id", "" + geo.id);
                            info.put("lat", locationNow.getLat());
                            info.put("lng", locationNow.getLng());
                            info.put("accuracy", locationNow.getAccuracy());
                            info.put("method", locationNow.getMethod());
                            Event event = new Event();
                            event.setName(transition);
                            event.setInfo(info.toString());
                            JSONObject jsonObjectStatus = new JSONObject();
                            dataSource.updateGeofenceType(geo.id, transition);
                            PreyLogger.d("________________geo id:"+geo.id+" event:" + transition.toString());
                            new EventThread(ctx, event, jsonObjectStatus, transition).start();
                        } catch (Exception e) {
                            PreyLogger.e("error:" + e.getMessage(), e);
                        }
                    }else{
                        dataSource.updateGeofenceType(geo.id, transition);
                    }
                }
            }
        }
    }
}