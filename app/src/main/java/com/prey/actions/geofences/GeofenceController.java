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
        try {
            GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
            listBD = dataSource.getAllGeofences();
            listWeb =null;
            try {listWeb = GeofecenceParse.getJSONFromUrl(ctx); } catch (Exception e) {}
            updateZones(ctx,listWeb,listBD,dataSource);
        } catch (Exception e) {
            PreyLogger.e("error run"+e.getMessage(),e);
        }
    }

    private void updateZones( Context ctx,List<GeofenceDto> listWeb ,List<GeofenceDto> listBD,GeofenceDataSource dataSource){
        try {
            List<GeofenceDto> listDelete=new ArrayList<>();
            List<GeofenceDto> listUpdate=new ArrayList<>();
            Map<String, GeofenceDto> mapBD = convertMap(listBD);
            Map<String, GeofenceDto> mapWeb = convertMap(listWeb);
            List<String> removeList = new ArrayList<String>();
            List<String> listRemove = new ArrayList<String>();
            List<GeofenceDto> listAdd = new ArrayList<GeofenceDto>();
            for(int i=0;listBD!=null&&i<listBD.size();i++){
                GeofenceDto dto=listBD.get(i);
                if(mapWeb!=null&&!mapWeb.containsKey(dto.getId())){
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
                PreyLogger.d("GEO infoDelete:" + infoDelete);
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
            List list=dataSource.getAllGeofences();
            if(list!=null&&list.size()>0) {
                initList(ctx, list);
            }
        } catch (Exception e) {
            PreyLogger.e("error run"+e.getMessage(),e);
        }
    }

    public void deleteAllZones(Context ctx){
        GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
        listBD = dataSource.getAllGeofences();
        for(int i=0;listBD!=null&&i<listBD.size();i++) {
            GeofenceDto dto = listBD.get(i);
            dataSource.deleteGeofence(dto.getId());
        }
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
        new Thread() {
            public void run() {
                try{
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, params);
                } catch (Exception e) {
                }
            }
        }.start();
    }

    public void initList(Context ctx,List<GeofenceDto> listBD) {
        int loiteringDelay= FileConfigReader.getInstance(ctx).getGeofenceLoiteringDelay();
        List<com.google.android.gms.location.Geofence> mGeofenceList  = new ArrayList<Geofence>();
        final List<GeofenceDto> listToBdAdd = new ArrayList<GeofenceDto>();
        String info = "[";
        for (int i = 0; listBD != null && i < listBD.size(); i++) {
            GeofenceDto geo = listBD.get(i);
            listToBdAdd.add(geo);
            PreyLogger.d("GEO START id:" + geo.name + " lat:" + geo.latitude + " long:" + geo.longitude + " ra:" + geo.radius +" type:"+ geo.type+ " expires:" + geo.expires);
            mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                    .setRequestId(geo.id)
                    .setCircularRegion(geo.latitude, geo.longitude, geo.radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(loiteringDelay) //30 seconds
                     .setNotificationResponsiveness(0)  //0 seconds
                    .build());
            info += geo.id;
            if (i + 1 < listBD.size()) {
                info += ",";
            }
        }
        info += "]";

        final String extraInfo = info;
        PreyLogger.d("GEO info:" + extraInfo);
        GeofencingRequest.Builder builderEnter = new GeofencingRequest.Builder();
        builderEnter.setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER|GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builderEnter.addGeofences(mGeofenceList );
        GeofencingRequest geofencingRequest = builderEnter.build();
        final Context ctx2=ctx;
        try {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(ctx, GeofenceIntentService.class);
                PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                LocationServices.getGeofencingClient(ctx).addGeofences(geofencingRequest,pendingIntent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        PreyLogger.d("GEO saveGeofence");
                                    }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        PreyLogger.d("GEO saveGeofence failed: " + e.getMessage());
                                        sendNotify(ctx2, UtilJson.makeMapParam("start", "geofencing", "failed", "status:" + e.getMessage()));
                                    }
                        });
            }//if
        } catch (Exception e) {
            PreyLogger.e("GEO error:" + e.getMessage(), e);
            sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "error:" + e.getMessage()));
        }
    }

    public synchronized static void verifyGeozone(Context ctx,PreyLocation locationNow){
        PreyLogger.d("GEO connection verifyGeozone");
        try{
            if(locationNow!=null && (locationNow.getLat()!=0 && locationNow.getLng()!=0)) {
                GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
                List<GeofenceDto> listBD = dataSource.getAllGeofences();
                PreyLogger.d("GEO listBD size:" + (listBD == null ? 0 : listBD.size()));
                int maximumAccuracy = PreyConfig.getPreyConfig(ctx).getGeofenceMaximumAccuracy();
                for (int i = 0; listBD != null && i < listBD.size(); i++) {
                    GeofenceDto geo = listBD.get(i);
                    validateGeozone(ctx, geo, maximumAccuracy, locationNow, dataSource);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void validateGeozone(Context ctx,GeofenceDto geo,int maximumAccuracy,PreyLocation locationNow,GeofenceDataSource dataSource){
        if(locationNow!=null&&locationNow.getAccuracy() < maximumAccuracy) {
            PreyLocation locationGeo = geo.getPreyLocation();
            double distance = LocationUtil.distance(locationNow,locationGeo);
            String transition="";
            if (distance > geo.getRadius()) {
                transition = GEOFENCING_OUT;
            } else {
                transition = GEOFENCING_IN;
            }
            dataSource.updateGeofenceType(geo.id, transition);
            PreyLogger.d("GEO validateGeozone name:"+geo.name+" type:"+geo.getType()+" transition:" + transition);
            if (!transition.equals(geo.getType())) {
                if(GEOFENCING_IN.equals(transition)||(GEOFENCING_OUT.equals(transition) && geo.getType()!=null)) {
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
                        PreyLogger.d("GEO name:"+geo.name+" event:" + transition.toString());
                        new EventThread(ctx, event, jsonObjectStatus, transition).start();
                    } catch (Exception e) {
                        PreyLogger.e("GEO error:" + e.getMessage(), e);
                    }
                }
            }
        }
    }
}