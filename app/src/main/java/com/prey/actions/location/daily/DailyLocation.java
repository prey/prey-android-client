/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily;

import android.content.Context;
import android.os.StrictMode;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.LocationUpdatesService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.location.PreyLocationManager;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Date;

public class DailyLocation {

    /**
     * Method checks if it should send a location
     *
     * @param context
     */
    public void run(Context context) {
        String dailyLocation = PreyConfig.getPreyConfig(context).getDailyLocation();
        String nowDailyLocation = PreyConfig.FORMAT_SDF_AWARE.format(new Date());
        if (!nowDailyLocation.equals(dailyLocation)) {
            PreyLocationManager.getInstance(context).setLastLocation(null);
            try {
                PreyLocationManager.getInstance(context).setLastLocation(null);
                new LocationUpdatesService().startForegroundService(context);
                PreyLocation preyLocation = null;
                int i = 0;
                while (i < LocationUtil.MAXIMUM_OF_ATTEMPTS) {
                    PreyLogger.d(String.format("DAILY getPreyLocationApp[%s]", i));
                    try {
                        Thread.sleep(LocationUtil.SLEEP_OF_ATTEMPTS[i] * 1000);
                    } catch (InterruptedException e) {
                        PreyLogger.e(String.format("DAILY error :%s", e.getMessage()), e);
                    }
                    preyLocation = PreyLocationManager.getInstance(context).getLastLocation();
                    if (preyLocation != null) {
                        preyLocation.setMethod("native");
                    } else {
                        PreyLogger.d(String.format("DAILY null[%s]", i));
                    }
                    if (preyLocation != null && preyLocation.getLat() != 0 && preyLocation.getLng() != 0) {
                        break;
                    }
                    i++;
                }
                if (preyLocation != null && preyLocation.getLat() != 0 && preyLocation.getLng() != 0) {
                    sendLocation(context, preyLocation);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            PreyLogger.d("DAILY location already sent");
        }
    }

    /**
     * Method that sends the location
     *
     * @param context
     * @param preyLocation
     */
    public static void sendLocation(Context context, PreyLocation preyLocation) throws Exception {
        double accD = Math.round(preyLocation.getAccuracy() * 100.0) / 100.0;
        JSONObject json = new JSONObject();
        String method = preyLocation.getMethod();
        if (method == null)
            method = "native";
        json.put("lat", preyLocation.getLat());
        json.put("lng", preyLocation.getLng());
        json.put("accuracy", accD);
        json.put("method", method);
        JSONObject location = new JSONObject();
        location.put("location", json);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        PreyHttpResponse preyResponse = PreyWebServices.getInstance().sendLocation(context, location);
        if (preyResponse != null) {
            int statusCode = preyResponse.getStatusCode();
            PreyLogger.d(String.format("DAILY getStatusCode :%s", statusCode));
            if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
                PreyConfig.getPreyConfig(context).setDailyLocation(PreyConfig.FORMAT_SDF_AWARE.format(new Date()));
            }
            PreyLogger.d(String.format("DAILY sendNowAware:%s", preyLocation.toString()));
        }
    }

}