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
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceController {

    private static GeofenceController INSTANCE;
    private GoogleApiClient mGoogleApiClient = null;
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

    public void run(final Context ctx,String messageId) {
        try {
            mGoogleApiClient = connectGoogleApiClient(ctx);
            dataSource = new GeofenceDataSource(ctx);
            listBD = dataSource.getAllGeofences();
            mapBD = convertMap(listBD);
            listWeb = GeofecenceParse.getJSONFromUrl(ctx);
            mapWeb = convertMap(listWeb);
            deleteZones(ctx,messageId);
            addZones(ctx,messageId);
        } catch (Exception e) {
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

    private void deleteZones(final Context ctx,String messageId) {
        List<String> removeList = new ArrayList<String>();
        for (int i = 0; listBD != null && i < listBD.size(); i++) {
            GeofenceDto geo = listBD.get(i);
            if (!mapWeb.containsKey(geo.getId())) {
                PreyLogger.d("remove id:" + geo.getId());
                dataSource.deleteGeofence(geo.getId());
                String lastEvent=PreyConfig.getPreyConfig(ctx).getLastEvent();
                if(lastEvent!=null&&lastEvent.indexOf( geo.getId())>=0) {
                    PreyConfig.getPreyConfig(ctx).setLastEventGeo("");
                }
                removeList.add(geo.getId());
            }
        }
        if (removeList != null && removeList.size() > 0) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeList);
            String infoDelete = "[";
            for (int i = 0; removeList != null && i < removeList.size(); i++) {
                infoDelete += removeList.get(i);
                if (i + 1 < removeList.size()) {
                    infoDelete += ",";
                }
            }
            infoDelete += "]";
            PreyLogger.d("infoDelete:" + infoDelete);
            sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "stopped", infoDelete,messageId));
        }
    }

    public void deleteAllZones(Context ctx) {
        List<String> removeList = new ArrayList<String>();
        for (int i = 0; listBD != null && i < listBD.size(); i++) {
            GeofenceDto geo = listBD.get(i);
            dataSource.deleteGeofence(geo.getId());
            String lastEvent=PreyConfig.getPreyConfig(ctx).getLastEvent();
            if(lastEvent!=null&&lastEvent.indexOf( geo.getId())>=0) {
                PreyConfig.getPreyConfig(ctx).setLastEventGeo("");
            }
            removeList.add(geo.getId());
        }
        if (removeList != null && removeList.size() > 0) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeList);
        }
    }

    public void sendNotify(final Context ctx, final Map<String, String> params) {
        new Thread() {
            public void run() {
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, params);
            }
        }.start();
    }


    private void addZones(final Context ctx,final String messageId) {
        List<com.google.android.gms.location.Geofence> mGeofenceList = new ArrayList<Geofence>();
        final List<GeofenceDto> listToBdAdd = new ArrayList<GeofenceDto>();
        String infoAdd = "[";
        for (int i = 0; listWeb != null && i < listWeb.size(); i++) {
            GeofenceDto geo = listWeb.get(i);
            if (!mapBD.containsKey(geo.getId())) {
                listToBdAdd.add(geo);
                PreyLogger.d("__[START]___________id:" + geo.name + " lat:" + geo.latitude + " long:" + geo.longitude + " ra:" + geo.radius + " expires:" + geo.expires);
                int transitionTypes = com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;

                mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                        .setRequestId(geo.id)
                        .setCircularRegion(geo.latitude, geo.longitude, geo.radius)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(transitionTypes)
                        .build());

                infoAdd += geo.id;
                if (i + 1 < listWeb.size()) {
                    infoAdd += ",";
                }
            }
        }

        infoAdd += "]";
        final String infoExtra = infoAdd;
        PreyLogger.d("infoAdd:" + infoExtra);

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        GeofencingRequest geofencingRequest = builder.build();
        if (mGoogleApiClient.isConnected()) {
            PreyLogger.d("---->isConnected");
            try {
                Intent intent = new Intent(ctx, GeofenceIntentService.class);
                PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(
                            mGoogleApiClient,
                            geofencingRequest,
                            pendingIntent
                    );
                    result.setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            PreyLogger.d("*********************connectionAddListener  status");
                            if (status.isSuccess()) {
                                PreyLogger.d("********saveGeofence");
                                sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "started", infoExtra,messageId));
                                GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
                                for (int i = 0; listToBdAdd != null && i < listToBdAdd.size(); i++) {
                                    dataSource.createGeofence(listToBdAdd.get(i));
                                }
                            } else {
                                PreyLogger.d("*********************Registering geofence failed: " + status.getStatusMessage() + " : " + status.getStatusCode());
                                sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "status:" + status.isSuccess(),messageId));
                            }
                        }
                    });
                }


            } catch (Exception e) {
                PreyLogger.e("error ---->isConnected:" + e.getMessage(), e);
                sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "error:" + e.getMessage(),messageId));
            }
        } else {
            PreyLogger.d("not connect mGoogleApiClient 3");
            sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "not connect mGoogleApiClient",messageId));
        }
    }




    public void init(final Context ctx,final String messageId) {
        PreyLogger.d("_GeofenceController__init");
        GoogleApiClient mGoogleApiClient = null;
        try {
            mGoogleApiClient = connectGoogleApiClient(ctx);
            GeofenceDataSource dataSource = new GeofenceDataSource(ctx);
            List<GeofenceDto> listBD = dataSource.getAllGeofences();
            List<com.google.android.gms.location.Geofence> mGeofenceList = new ArrayList<Geofence>();
            final List<GeofenceDto> listToBdAdd = new ArrayList<GeofenceDto>();
            String info = "[";
            for (int i = 0; listBD != null && i < listBD.size(); i++) {
                GeofenceDto geo = listBD.get(i);
                listToBdAdd.add(geo);
                PreyLogger.d("__[START]___________id:" + geo.name + " lat:" + geo.latitude + " long:" + geo.longitude + " ra:" + geo.radius + " expires:" + geo.expires);
                int transitionTypes = com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
                mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                        .setRequestId(geo.id)
                        .setCircularRegion(geo.latitude, geo.longitude, geo.radius)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(transitionTypes)
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
            if (mGoogleApiClient.isConnected()) {
                PreyLogger.d("---->isConnected");
                try {
                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(ctx, GeofenceIntentService.class);
                        PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(
                                mGoogleApiClient,
                                geofencingRequest,
                                pendingIntent
                        );
                        result.setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                PreyLogger.d("*********************connectionAddListener  status :" + status);
                                if (status.isSuccess()) {
                                    PreyLogger.d("********saveGeofence");
                                } else {
                                    PreyLogger.d("*********************Registering geofence failed: " + status.getStatusMessage() + " : " + status.getStatusCode());
                                    sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "status:" + status.isSuccess(),messageId));
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    PreyLogger.e("error ---->isConnected:" + e.getMessage(), e);
                    sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "error:" + e.getMessage(),messageId));
                }
            } else {
                PreyLogger.d("not connect mGoogleApiClient 3");
                sendNotify(ctx, UtilJson.makeMapParam("start", "geofencing", "failed", "not connect mGoogleApiClient",messageId));
            }
        } catch (Exception e) {
        }
    }

    private GoogleApiClient connectGoogleApiClient(Context ctx) {
        GoogleApiClient mGoogleApiClient = null;
        try {
            mGoogleApiClient = buildGoogleApiClient(ctx);
            int i = 0;
            while (i < 50 && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
                i++;
                Thread.sleep(1000);
                if (i % 10 == 0) {
                    buildGoogleApiClient(ctx);
                }
                PreyLogger.d("___[" + i + "] sleep");
            }
        } catch (Exception e) {
            PreyLogger.e("error:" + e.getMessage(), e);
        }
        return mGoogleApiClient;
    }

    private void disconnectGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        try {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
        } catch (Exception e) {
            PreyLogger.e("error:" + e.getMessage(), e);
        }
    }

    private synchronized GoogleApiClient buildGoogleApiClient(Context ctx) {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        PreyLogger.d("________________Connected to GoogleApiClient");
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        PreyLogger.d("________________Connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        PreyLogger.d("________________Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
                    }
                })
                .addApi(LocationServices.API)
                .build();
        return mGoogleApiClient;
    }
}