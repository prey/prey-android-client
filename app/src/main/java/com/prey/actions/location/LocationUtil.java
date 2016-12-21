/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.PreyPhone.Wifi;
import com.prey.actions.HttpDataService;
import com.prey.json.UtilJson;
import com.prey.managers.PreyWifiManager;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;

public class LocationUtil {

    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String ACC = "accuracy";
    public static final String METHOD = "method";

    public static HttpDataService dataLocation(Context ctx, String messageId, boolean asynchronous) {
        HttpDataService data = null;
        try {
            PreyLocation preyLocation = getLocation(ctx,messageId,asynchronous);
            if (!asynchronous) {
                if (preyLocation != null) {
                    PreyLogger.d("locationData:" + preyLocation.getLat() + " " + preyLocation.getLng() + " " + preyLocation.getAccuracy());
                    data = convertData(preyLocation);
                } else {
                    sendNotify(ctx, "Error", "failed");
                }
            }else{
                return null;
            }
        } catch (Exception e) {
            sendNotify(ctx, "Error", messageId);
        }
        return data;
    }

    public static PreyLocation getLocation(Context ctx, String messageId, boolean asynchronous) throws Exception{
        PreyLocation preyLocation = null;
        boolean isGpsEnabled = PreyLocationManager.getInstance(ctx).isGpsLocationServiceActive();
        boolean isNetworkEnabled = PreyLocationManager.getInstance(ctx).isNetworkLocationServiceActive();
        boolean isWifiEnabled = PreyWifiManager.getInstance(ctx).isWifiEnabled();
        boolean isGooglePlayServicesAvailable=isGooglePlayServicesAvailable(ctx);
        PreyLogger.d("status gps:" + isGpsEnabled + " net:" + isNetworkEnabled + " wifi:" + isWifiEnabled+" play:"+isGooglePlayServicesAvailable);
        String method = getMethod(isGpsEnabled, isNetworkEnabled);
        try {
            if (isWifiEnabled) {
                preyLocation = getDataLocationWifi(ctx,method,asynchronous,preyLocation);
            }
        } catch (Exception e) {
        }
        try {
            if(!isGooglePlayServicesAvailable||(isGpsEnabled&&!isNetworkEnabled)) {
                preyLocation = getPreyLocationAppService(ctx,method,asynchronous,preyLocation);
            }else{
                preyLocation = getPreyLocationPlayService(ctx, method, asynchronous,preyLocation);
            }
        } catch (Exception e) {
            throw e;
        }
        return preyLocation;
    }

    private static boolean isGooglePlayServicesAvailable(Context ctx){
        boolean isGooglePlayServicesAvailable=false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
            if (ConnectionResult.SUCCESS == resultCode) {
                isGooglePlayServicesAvailable=true;
            }
        }
        return isGooglePlayServicesAvailable;
    }

    private static String getMethod(boolean isGpsEnabled, boolean isNetworkEnabled) {
        if (isGpsEnabled && isNetworkEnabled) {
            return "native";
        }
        if (isGpsEnabled) {
            return "gps";
        }
        if (isNetworkEnabled) {
            return "network";
        }
        return "";
    }

    private static void sendNotify(Context ctx, String message) {
        Map<String, String> parms = UtilJson.makeMapParam("get", "location", "failed", message);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, parms);
    }

    private static void sendNotify(Context ctx, String message, String status) {
        Map<String, String> parms = UtilJson.makeMapParam("get", "location", status, message);
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, parms);
    }

    private static PreyLocation getDataLocationWifi(Context ctx, String method, boolean asynchronous, PreyLocation preyLocationOld) throws Exception {
        PreyLocation preyLocation = null;
        List<Wifi> listWifi = null;
        PreyPhone preyPhone = new PreyPhone(ctx);
        if (PreyWifiManager.getInstance(ctx).isWifiEnabled()) {
            listWifi = preyPhone.getListWifi();
        }
        if (listWifi == null || listWifi.size() == 0) {
            throw new Exception();
        }
        preyLocation = PreyWebServices.getInstance().getLocation(ctx, listWifi);
        preyLocation.setMethod("wifi");
        sendLocation(ctx, asynchronous, null, preyLocation);
        return preyLocation;
    }

    private final static int MAXIMUM_OF_ATTEMPTS=9;
    private final static int []SLEEP_OF_ATTEMPTS=new int[]{2,2,2,3,3,3,4,4,4};

    private static PreyLocation getPreyLocationPlayService(final Context ctx, String method, boolean asynchronous, PreyLocation preyLocationOld) throws Exception {
        PreyLocation preyLocation = null;
        final PreyGooglePlayServiceLocation play = new PreyGooglePlayServiceLocation();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    play.init(ctx);
                }
            }).start();
            Location currentLocation = null;
            PreyLocationManager manager =PreyLocationManager.getInstance(ctx);
            int i = 0;
            while (i < MAXIMUM_OF_ATTEMPTS) {
                try {
                    Thread.sleep(SLEEP_OF_ATTEMPTS[i]*1000);
                } catch (InterruptedException e) {
                }
                currentLocation = play.getLastLocation(ctx);
                if (currentLocation != null) {
                    PreyLogger.d("getPreyLocationPlayService["+i+"]:"+currentLocation.toString());
                    preyLocation = new PreyLocation(currentLocation, method);
                    preyLocationOld = sendLocation(ctx, asynchronous, preyLocationOld, preyLocation);
                    if(!asynchronous)
                        i=MAXIMUM_OF_ATTEMPTS;
                }
                i++;
            }
        } catch (Exception e) {
            PreyLogger.d("Error getPreyLocationPlayService:"+e.getMessage());
            throw e;
        } finally {
            play.stopLocationUpdates();
        }
        return preyLocation;
    }

    private static PreyLocation getPreyLocationAppService(final Context ctx, String method, boolean asynchronous, PreyLocation preyLocationOld) throws Exception {
        PreyLocation preyLocation = null;
        Intent intent = new Intent(ctx, LocationService.class);
        try {
            ctx.startService(intent);
            int i = 0;
            while (i < MAXIMUM_OF_ATTEMPTS) {
                try {
                    Thread.sleep(SLEEP_OF_ATTEMPTS[i]*1000);
                } catch (InterruptedException e) {
                }
                PreyLocation location = PreyLocationManager.getInstance(ctx).getLastLocation();
                if (location.isValid()) {
                    preyLocation=location;
                    preyLocation.setMethod(method);
                    PreyLogger.d("getPreyLocationAppService["+i+"]:"+preyLocation.toString());
                    preyLocationOld = sendLocation(ctx, asynchronous, preyLocationOld, preyLocation);
                    if(!asynchronous)
                        i=MAXIMUM_OF_ATTEMPTS;
                }
                i++;
            }
        } catch (Exception e) {
            PreyLogger.d("Error getPreyLocationAppService:"+e.getMessage());
            throw e;
        } finally {
            ctx.stopService(intent);
        }
        return preyLocation;
    }

    private static PreyLocation sendLocation(Context ctx,boolean asynchronous, PreyLocation locationOld, PreyLocation locationNew){
        double distance = distance(locationOld,locationNew);
        double distanceLocation = PreyConfig.getPreyConfig(ctx).getDistanceLocation();
        if(locationNew!=null) {
            if(locationOld==null||distance>distanceLocation||locationOld.getAccuracy()>locationNew.getAccuracy()){
                if (asynchronous) {
                    HttpDataService data = convertData(locationNew);
                    ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
                    dataToBeSent.add(data);
                    PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
                }
            }
            return locationNew;
        }else {
            return locationOld;
        }
    }

    private static double distance(PreyLocation locationOld, PreyLocation locationNew){
        if(locationOld!=null&&locationNew!=null) {
            Location locStart = new Location("");
            locStart.setLatitude(locationNew.getLat());
            locStart.setLongitude(locationNew.getLng());
            Location locEnd = new Location("");
            locEnd.setLatitude(locationOld.getLat());
            locEnd.setLongitude(locationOld.getLng());
            return Math.round(locStart.distanceTo(locEnd));
        }else{
            return 0d;
        }
    }

    public static HttpDataService convertData(PreyLocation lastLocation) {
        if(lastLocation==null)
            return null;
        HttpDataService data = new HttpDataService("location");
        data.setList(true);
        HashMap<String, String> parametersMap = new HashMap<String, String>();
        parametersMap.put(LAT, Double.toString(lastLocation.getLat()));
        parametersMap.put(LNG, Double.toString(lastLocation.getLng()));
        parametersMap.put(ACC, Float.toString(Math.round(lastLocation.getAccuracy())));
        parametersMap.put(METHOD, lastLocation.getMethod() );
        data.addDataListAll(parametersMap);
        PreyLogger.i("lat:"+lastLocation.getLat()+" lng:"+lastLocation.getLng()+" acc:"+lastLocation.getAccuracy()+" met:"+lastLocation.getMethod());
        return data;
    }

    public static PreyLocation dataPreyLocation(Context ctx,String messageId) {
        HttpDataService data=dataLocation(ctx,messageId,false);
        PreyLocation location=new PreyLocation();
        location.setLat(Double.parseDouble(data.getDataList().get(LAT)));
        location.setLng(Double.parseDouble(data.getDataList().get(LNG)));
        location.setAccuracy(Float.parseFloat(data.getDataList().get(ACC)));
        return location;
    }
}