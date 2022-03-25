/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class FileConfigReader {

    private static FileConfigReader _instance = null;
    private Properties properties;

    private FileConfigReader(Context ctx) {
        try {
            PreyLogger.d("Loading config properties from file...");
            properties = new Properties();
            InputStream is = ctx.getResources().openRawResource(R.raw.config);
            properties.load(is);
            is.close();
            PreyLogger.d("Config: " + properties);

        } catch (NotFoundException e) {
            PreyLogger.e("Config file wasn't found", e);
        } catch (IOException e) {
            PreyLogger.e("Couldn't read config file", e);
        }
    }

    public static FileConfigReader getInstance(Context ctx) {
        if (_instance == null)
            _instance = new FileConfigReader(ctx);
        return _instance;
    }

    public String getPreyCampaign() {
        return properties.getProperty("prey-campaign");
    }

    public String getPreyPanel() {
        return properties.getProperty("prey-panel");
    }

    public String getAgreementId() {
        return properties.getProperty("agreement-id");
    }

    public String getGcmId() {
        return properties.getProperty("gcm-id");
    }

    public String getGcmIdPrefix() {
        return properties.getProperty("gcm-id-prefix");
    }

    public String getc2dmAction() {
        return properties.getProperty("c2dm-action");
    }

    public String getc2dmMessageSync() {
        return properties.getProperty("c2dm-message-sync");
    }

    public String getPreyDomain() {
        return properties.getProperty("prey-domain");
    }

    public String getPreySubdomain() {
        return properties.getProperty("prey-subdomain");
    }

    public String getPreyUninstall() {
        return properties.getProperty("prey-uninstall");
    }

    public String getPreyUninstallEs() {
        return properties.getProperty("prey-uninstall-es");
    }

    public String getPreyMinorVersion() {
        return properties.getProperty("prey-minor-version");
    }

    public boolean isAskForPassword() {
        return Boolean.parseBoolean(properties.getProperty("ask-for-password"));
    }

    public boolean isLogEnabled() {
        return Boolean.parseBoolean(properties.getProperty("log-enabled"));
    }

    public String getEmailFeedback() {
        return properties.getProperty("email-feedback");
    }

    public String getSubjectFeedback() {
        return properties.getProperty("subject-feedback");
    }

    public String getApiV2() {
        return properties.getProperty("api-v2");
    }

    public boolean isScheduled() {
        return Boolean.parseBoolean(properties.getProperty("scheduled"));
    }

    public boolean isOverOtherApps() {
        return Boolean.parseBoolean(properties.getProperty("over-other-apps"));
    }

    public int getMinuteScheduled() {
        return Integer.parseInt(properties.getProperty("minute-scheduled"));
    }

    public int getTimeoutReport() {
        return Integer.parseInt(properties.getProperty("timeout-report"));
    }

    public int getGeofenceMaximumAccuracy() {
        return Integer.parseInt(properties.getProperty("geofence-maximum-accuracy"));
    }

    public String getPreyJwt() {
        return properties.getProperty("prey-jwt");
    }

    public String getPreyGooglePlay(){
        return properties.getProperty("prey-google-play");
    }

    public int getGeofenceLoiteringDelay() {
        return Integer.parseInt(properties.getProperty("geofence-loitering-delay"));
    }

    public String getPreyEventsLogs() {
        return properties.getProperty("prey-events-logs");
    }

    public int getDistanceLocation() {
        return Integer.parseInt(properties.getProperty("distance-location"));
    }

    public int getGeofenceNotificationResponsiveness() {
        return Integer.parseInt(properties.getProperty("geofence-notification-responsiveness"));
    }

    public String getFlyerKey() {
        return properties.getProperty("flyer-key");
    }

    public int getDistanceAware() {
        return Integer.parseInt(properties.getProperty("distance-aware"));
    }

    public int getRadiusAware() {
        return Integer.parseInt(properties.getProperty("radius-aware"));
    }

    public String getPreyTerms() {
        return properties.getProperty("prey-terms");
    }

    public String getPreyTermsEs() {
        return properties.getProperty("prey-terms-es");
    }

    public String getPreyForgot() {
        return properties.getProperty("prey-forgot");
    }

    /**
     * Method if it should show pin
     * @return true o false
     */
    public boolean getOpenPin() {
        return Boolean.parseBoolean(properties.getProperty("open-pin"));
    }
}