/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;

public class FileConfigReader {

    private static FileConfigReader _instance = null;

    private FileConfigReader(Context ctx) {
        PreyLogger.d("Config BuildConfig PREY_PANEL=" + BuildConfig.PREY_PANEL);
    }

    public static FileConfigReader getInstance(Context ctx) {
        if (_instance == null)
            _instance = new FileConfigReader(ctx);
        return _instance;
    }

    public String getPreyCampaign() {
        return BuildConfig.PREY_CAMPAIGN;
    }

    public String getPreyPanel() {
        return BuildConfig.PREY_PANEL;
    }

    public String getGcmIdPrefix() {
        return BuildConfig.GCM_ID_PREFIX;
    }

    public String getPreyDomain() {
        return BuildConfig.PREY_DOMAIN;
    }

    public String getPreySubdomain() {
        return BuildConfig.PREY_SUBDOMAIN;
    }

    public String getApiV2() {
        return BuildConfig.API_V2;
    }

    public boolean isScheduled() {
        return BuildConfig.SCHEDULED;
    }

    public int getMinuteScheduled() {
        return BuildConfig.MINUTE_SCHEDULED;
    }

    public int getTimeoutReport() {
        return BuildConfig.TIMEOUT_REPORT;
    }

    public int getGeofenceMaximumAccuracy() {
        return BuildConfig.GEOFENCE_MAXIMUM_ACCURACY;
    }

    public String getPreyJwt() {
        return BuildConfig.PREY_JWT;
    }

    public String getPreyGooglePlay(){
        return BuildConfig.PREY_GOOGLE_PLAY;
    }

    public int getGeofenceLoiteringDelay() {
        return BuildConfig.GEOFENCE_LOITERING_DELAY;
    }

    public int getDistanceLocation() {
        return BuildConfig.DISTANCE_LOCATION;
    }

    public int getGeofenceNotificationResponsiveness() {
        return BuildConfig.GEOFENCE_NOTIFICATION_RESPONSIVENESS;
    }

    public int getDistanceAware() {
        return BuildConfig.DISTANCE_AWARE;
    }

    public int getRadiusAware() {
        return BuildConfig.RADIUS_AWARE;
    }

    public String getPreyTerms() {
        return BuildConfig.PREY_TERMS;
    }

    public String getPreyTermsEs() {
        return BuildConfig.PREY_TERMS_ES;
    }

    public String getPreyForgot() {
        return BuildConfig.PREY_FORGOT;
    }

    /**
     * Method if it should show pin
     * @return true o false
     */
    public boolean getOpenPin() {
        return BuildConfig.OPEN_PIN;
    }
}
