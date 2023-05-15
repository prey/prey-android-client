/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.geofences.GeofenceController;
import com.prey.json.UtilJson;
import com.prey.managers.PreyWifiManager;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;

public class LocationUtil {

    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String ACC = "accuracy";
    public static final String METHOD = "method";

    public static HttpDataService dataLocation(final Context ctx, String messageId, boolean asynchronous) {
        return dataLocation(ctx, messageId, asynchronous, MAXIMUM_OF_ATTEMPTS);
    }

    public static HttpDataService dataLocation(final Context ctx, String messageId, boolean asynchronous, int maximum) {
        HttpDataService data = null;
        try {
            final PreyLocation preyLocation = getLocation(ctx, messageId, asynchronous, maximum);
            if (preyLocation != null && (preyLocation.getLat() != 0 && preyLocation.getLng() != 0)) {
                PreyLogger.d(String.format("locationData:%s %s %s", preyLocation.getLat(), preyLocation.getLng(), preyLocation.getAccuracy()));
                PreyConfig.getPreyConfig(ctx).setLocation(preyLocation);
                data = convertData(preyLocation);
            } else {
                PreyLogger.d("locationData else:");
                return null;
            }
        } catch (Exception e) {
            sendNotify(ctx, "Error", messageId);
        }
        return data;
    }

    public static PreyLocation getLocation(Context ctx, String messageId, boolean asynchronous) throws Exception{
        return getLocation(ctx, messageId, asynchronous, MAXIMUM_OF_ATTEMPTS);
    }

    public static PreyLocation getLocation(Context ctx, String messageId, boolean asynchronous, int maximum) throws Exception{
        PreyLocation preyLocation = null;
        boolean isGpsEnabled = PreyLocationManager.getInstance(ctx).isGpsLocationServiceActive();
        boolean isNetworkEnabled = PreyLocationManager.getInstance(ctx).isNetworkLocationServiceActive();
        boolean isWifiEnabled = PreyWifiManager.getInstance(ctx).isWifiEnabled();
        boolean isGooglePlayServicesAvailable=isGooglePlayServicesAvailable(ctx);
        String locationInfo="{\"gps\":" + isGpsEnabled + ",\"net\":" + isNetworkEnabled + ",\"wifi\":" + isWifiEnabled+",\"play\":"+isGooglePlayServicesAvailable+"}";
        PreyConfig.getPreyConfig(ctx).setLocationInfo(locationInfo);
        PreyLogger.d(locationInfo);
        String method = getMethod(isGpsEnabled, isNetworkEnabled);
        try {
            preyLocation = getPreyLocationAppService(ctx, method, asynchronous, preyLocation, maximum);
        } catch (Exception e) {
            PreyLogger.e("Error PreyLocationApp:"+e.getMessage(),e);
        }
        try {
            if(preyLocation==null||preyLocation.getLocation()==null||(preyLocation.getLocation().getLatitude()==0&&preyLocation.getLocation().getLongitude()==0)) {
                preyLocation = getPreyLocationAppServiceOreo(ctx, method, asynchronous, preyLocation);
            }
        } catch (Exception e) {
            PreyLogger.e("Error AppServiceOreo:"+e.getMessage(),e);
        }
        if (preyLocation != null) {
            PreyLogger.d("preyLocation lat:" + preyLocation.getLat() + " lng:" + preyLocation.getLng());
        }
        final PreyLocation location = preyLocation;
        new Thread() {
            public void run() {
                GeofenceController.verifyGeozone(ctx, location);
            }
        }.start();
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

    public final static int MAXIMUM_OF_ATTEMPTS=6;
    public final static int MAXIMUM_OF_ATTEMPTS2=6;
    public final static int []SLEEP_OF_ATTEMPTS=new int[]{1,1,1,1,2,2,2,2,2,2};

    public static PreyLocation getPreyLocationPlayService(final Context ctx, String method, boolean asynchronous, PreyLocation preyLocationOld) throws Exception {
        PreyLocation preyLocation = null;
        PreyLogger.d("getPreyLocationPlayService");
        final PreyGooglePlayServiceLocation play = new PreyGooglePlayServiceLocation();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    play.init(ctx);
                }
            }).start();
            Location currentLocation = null;
            PreyLocationManager manager = PreyLocationManager.getInstance(ctx);
            currentLocation = play.getLastLocation(ctx);
            if (currentLocation != null) {
                PreyLogger.d(String.format("currentLocation:%s", currentLocation.toString()));
                preyLocation = new PreyLocation(currentLocation, method);
            }
        } catch (Exception e) {
            PreyLogger.d(String.format("Error getPreyLocationPlayService:%s", e.getMessage()));
            throw e;
        } finally {
            if (play != null)
                play.stopLocationUpdates();
        }
        return preyLocation;
    }

    public static PreyLocation getPreyLocationAppServiceOreo(final Context ctx, String method, boolean asynchronous, PreyLocation preyLocationOld) {
        PreyLocation preyLocation = null;
        Intent intentLocation = new Intent(ctx, LocationUpdatesService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startService(intentLocation);
            } else {
                ctx.startService(intentLocation);
            }
            int i = 0;
            PreyLocationManager.getInstance(ctx).setLastLocation(null);
            while (i < MAXIMUM_OF_ATTEMPTS2) {
                PreyLogger.d(String.format("getPreyLocationAppServiceOreo[%d]:", i));
                try {
                    Thread.sleep(SLEEP_OF_ATTEMPTS[i] * 1000);
                } catch (Exception e) {
                    i = MAXIMUM_OF_ATTEMPTS2;
                    break;
                }
                preyLocation = PreyLocationManager.getInstance(ctx).getLastLocation();
                if (preyLocation != null) {
                    preyLocation.setMethod(method);
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error getPreyLocationAppServiceOreo:%s", e.getMessage()), e);
            throw e;
        } finally {
            ctx.stopService(intentLocation);
        }
        return preyLocation;
    }

    private static PreyLocation getPreyLocationAppService(final Context ctx, String method, boolean asynchronous, PreyLocation preyLocationOld, int maximum) throws Exception {
        PreyLocation preyLocation = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
                (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            try {
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx);
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    PreyLocationManager.getInstance(ctx).setLastLocation(new PreyLocation(location));
                                }
                            }
                        });
                preyLocation = waitLocation(ctx, method, asynchronous, maximum);
            } catch (Exception e) {
                PreyLogger.e(String.format("getPreyLocationAppService e:%s", e.getMessage()), e);
            }
        } else {
            Intent intentLocation = new Intent(ctx, LocationService.class);
            try {
                ctx.startService(intentLocation);
                preyLocation = waitLocation(ctx, method, asynchronous, maximum);
            } catch (Exception e) {
                PreyLogger.e(String.format("getPreyLocationAppService e:%s", e.getMessage()), e);
                throw e;
            } finally {
                ctx.stopService(intentLocation);
            }
        }
        return preyLocation;
    }

    public static PreyLocation waitLocation(final Context ctx, String method, boolean asynchronous, int maximum) {
        PreyLocation preyLocation = null;
        PreyLocation location = PreyLocationManager.getInstance(ctx).getLastLocation();
        if (location != null && location.isValid()) {
            preyLocation = location;
            preyLocation.setMethod(method);
            PreyLogger.d(String.format("getPreyLocationAppService:%s", preyLocation.toString()));
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

    public static double distance(PreyLocation locationOld, PreyLocation locationNew){
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
        PreyLogger.d(String.format("lat:%.2f lng:%.2f acc:%.2f method:%s",lastLocation.getLat(),lastLocation.getLng(),lastLocation.getAccuracy(),lastLocation.getMethod()));
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