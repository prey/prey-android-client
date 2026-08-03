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
import com.prey.PreyPhone;
import com.prey.actions.location.LocationUpdatesService;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.location.PreyLocationManager;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DailyLocation {

    /** Maximum attempts to obtain a fix (widens the acquisition window). */
    private static final int MAXIMUM_OF_ATTEMPTS = 6;

    /** Seconds to wait before reading each attempt. */
    private static final int[] SLEEP_OF_ATTEMPTS = new int[]{2, 2, 3, 3, 4, 4};

    /** Accuracy (meters) considered good enough to stop early. */
    private static final float GOOD_ACCURACY_METERS = 50f;

    /**
     * @return a UTC {@code yyyy-MM-dd} formatter so the "already sent today" boundary is
     * evaluated in UTC rather than the device-local timezone.
     */
    private static SimpleDateFormat utcDayFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    /**
     * Method checks if it should send a location
     *
     * @param context
     */
    public void run(Context context) {
        Date now = new Date();
        String dailyLocation = PreyConfig.getPreyConfig(context).getDailyLocation();
        String nowDailyLocation = utcDayFormat().format(now);
        boolean isAirplaneModeOn = PreyPhone.isAirplaneModeOn(context);
        String schedule = PreyConfig.getPreyConfig(context).getLocationSchedule();
        boolean withinWindow = LocationScheduleWindow.isWithinAllowedWindow(schedule, now);
        PreyLogger.d(String.format("DailyLocation run isAirplaneModeOn:%s withinWindow:%s", isAirplaneModeOn, withinWindow));
        if (nowDailyLocation.equals(dailyLocation)) {
            PreyLogger.d("DAILY location already sent");
            return;
        }
        if (isAirplaneModeOn || !withinWindow) {
            PreyLogger.d("DAILY skipped: airplane mode or outside allowed window");
            return;
        }
        try {
            PreyLocationManager.getInstance(context).setLastLocation(null);
            new LocationUpdatesService().startForegroundService(context);
            PreyLocation bestLocation = null;
            for (int i = 0; i < MAXIMUM_OF_ATTEMPTS; i++) {
                PreyLogger.d(String.format("DAILY getPreyLocationApp[%s]", i));
                try {
                    Thread.sleep(SLEEP_OF_ATTEMPTS[i] * 1000L);
                } catch (InterruptedException e) {
                    PreyLogger.e(String.format("DAILY error :%s", e.getMessage()), e);
                }
                PreyLocation preyLocation = PreyLocationManager.getInstance(context).getLastLocation();
                if (preyLocation == null || preyLocation.getLat() == 0 || preyLocation.getLng() == 0) {
                    PreyLogger.d(String.format("DAILY null[%s]", i));
                    continue;
                }
                preyLocation.setMethod("native");
                // Keep the most accurate (lowest accuracy value) fix seen so far.
                if (bestLocation == null || preyLocation.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = preyLocation;
                }
                if (bestLocation.getAccuracy() > 0 && bestLocation.getAccuracy() <= GOOD_ACCURACY_METERS) {
                    break;
                }
            }
            // Send the best fix obtained; a mediocre fix still beats skipping the day.
            if (bestLocation != null && bestLocation.getLat() != 0 && bestLocation.getLng() != 0) {
                sendLocation(context, bestLocation);
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("DAILY run error:%s", e.getMessage()), e);
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
        json.put("force", true);
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
                PreyConfig.getPreyConfig(context).setDailyLocation(utcDayFormat().format(new Date()));
            }
            PreyLogger.d(String.format("DAILY sendNowAware:%s", preyLocation.toString()));
        }
    }

}