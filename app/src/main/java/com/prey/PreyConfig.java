/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/

package com.prey;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.prey.actions.location.PreyLocation;
import com.prey.activities.FeedbackActivity;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.UtilConnection;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PreyConfig {

    //Set false in production
    public static final boolean LOG_DEBUG_ENABLED = false;

    private static PreyConfig cachedInstance = null;

    public static final String TAG = "PREY";

    private static final String HTTP="https://";

    public static final String VERSION_PREY_DEFAULT="2.2.7";

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // Set to 1000 * 60 in production.
    public static final long DELAY_MULTIPLIER = 60 * 1000;

    // the minimum time interval for GPS notifications, in milliseconds (default 60000).
    public static final long UPDATE_INTERVAL = 60 * MILLISECONDS_PER_SECOND;

    // the minimum distance interval for GPS notifications, in meters (default 20)
    public static final float LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE = 20;

    // max "age" in ms of last location (default 120000).
    public static final long LAST_LOCATION_MAX_AGE = 30 * MILLISECONDS_PER_SECOND;

    public static final int LOCATION_PRIORITY_HIGHT = LocationRequest.PRIORITY_HIGH_ACCURACY;

    public static final long FASTEST_INTERVAL  =  40 * MILLISECONDS_PER_SECOND;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10 * MILLISECONDS_PER_SECOND;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =  UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static final String PROTECT_ACCOUNT="PROTECT_ACCOUNT";
    public static final String PROTECT_PRIVILEGES="PROTECT_PRIVILEGES";
    public static final String PROTECT_TOUR="PROTECT_TOUR";
    public static final String PROTECT_READY="PROTECT_READY";

    public static final String PREFS_SIM_SERIAL_NUMBER = "PREFS_SIM_SERIAL_NUMBER";
    public static final String PREFS_SECURITY_PROMPT_SHOWN = "PREFS_SECURITY_PROMPT_SHOWN";
    public static final String PREFS_IS_MISSING="PREFS_IS_MISSING";

    public static final String PREFS_DISABLE_POWER_OPTIONS="PREFS_DISABLE_POWER_OPTIONS";
    public static final String PREFS_BLOCK_APP_UNINSTALL="PREFS_BLOCK_APP_UNINSTALL";
    public static final String PREFS_RUN_BACKGROUND="PREFS_RUN_BACKGROUND";
    public static final String PREFS_BACKGROUND="PREFS_BACKGROUND";
    public static final String IS_LOCK_SET="IS_LOCK_SET";
    public static final String NEXT_ALERT="NEXT_ALERT";
    public static final String IS_CAMOUFLAGE_SET="IS_CAMOUFLAGE_SET";
    public static final String PREFS_RINGTONE="PREFS_RINGTONE";

    public static final String LAST_EVENT="LAST_EVENT";
    public static final String LOW_BATTERY_DATE="LOW_BATTERY_DATE";
    public static final String PREVIOUS_SSID="PREVIOUS_SSID";

    public static final String ERROR="ERROR";

    public static final String FLAG_FEEDBACK="FLAG_FEEDBACK";
    public static final String INSTALLATION_DATE="INSTALLATION_DATE";

    public static final String PREFS_ACCOUNT_VERIFIED="PREFS_ACCOUNT_VERIFIED";
    public static final String EMAIL="EMAIL";
    public static final String TWO_STEP="TWO_STEP";


    public static final String SEND_DATA="SEND_DATA";
    public static final String SCHEDULED="SCHEDULED";
    public static final String MINUTE_SCHEDULED="MINUTE_SCHEDULED2";

    public static final String IS_REVOKED_PASSWORD="IS_REVOKED_PASSWORD";
    public static final String REVOKED_PASSWORD="REVOKED_PASSWORD";
    public static final String NOTIFICATION_ID="NOTIFICATION_ID";
    public static final String INTERVAL_REPORT="INTERVAL_REPORT";
    public static final String EXCLUDE_REPORT="EXCLUDE_REPORT";
    public static final String LAST_REPORT_START_DATE="LAST_REPORT_START_DATE";
    public static final String TIMEOUT_REPORT="TIMEOUT_REPORT";
    public static final String INTERVAL_AWARE="INTERVAL_AWARE";
    public static final String TIME_SECURE_LOCK="TIME_SECURE_LOCK";
    public static final String LAST_TIME_SECURE_LOCK="LAST_TIME_SECURE_LOCK";

    public static final String LOCATION_LOW_BATTERY_DATE="LOCATION_LOW_BATTERY_DATE";
    public static final String SESSION_ID="SESSION_ID";

    public static final String PIN_NUMBER2="PIN_NUMBER2";
    public static final String PIN_NUMBER_ACTIVATE="PIN_NUMBER_ACTIVATE";
    public static final String SMS_COMMAND="SMS_COMMAND";
    public static final String PREFERENCE_LOCATION_LOW_BATTERY="PREFERENCE_LOCATION_LOW_BATTERY";

    public static final String TOKEN_JWT="TOKEN_JWT";

    public static final int ANDROID_INIT = 2000;
    public static final int ANDROID_SIGN_UP = 2001;
    public static final int ANDROID_TOUR_SCREEN = 2002;
    public static final int ANDROID_TOUR_COMPLETED = 2003;
    public static final int ANDROID_PRIVILEGES_GIVEN = 2004;
    public static final int ANDROID_SIGN_IN = 2005;
    public static final int ANDROID_LOGIN_SETTINGS = 2007;
    public static final int ANDROID_FAILED_LOGIN_SETTINGS = 2008;
    public static final int ANDROID_VERSION_UPDATED = 2009;
    public static final int ANDROID_ONBOARDING_INIT = 2010;
    public static final int ANDROID_ONBOARDING_COMPLETED = 2011;





    public static final String PREY_VERSION="PREY_VERSION";
    public static final String API_KEY="API_KEY";
    public static final String DEVICE_ID = "DEVICE_ID";
    public static final String ACCOUNT = "ACCOUNT";

    public static final String SIM_SERIAL_NUMBER = "SIM_SERIAL_NUMBER";


    public static final String CAN_ACCESS_FINE_LOCATION = "CAN_ACCESS_FINE_LOCATION";
    public static final String CAN_ACCESS_COARSE_LOCATION = "CAN_ACCESS_COARSE_LOCATION";
    public static final String CAN_ACCESS_CAMARA = "CAN_ACCESS_CAMARA";
    public static final String CAN_ACCESS_READ_PHONE_STATE = "CAN_ACCESS_READ_PHONE_STATE";
    public static final String CAN_ACCESS_EXTERNAL_STORAGE= "CAN_ACCESS_EXTERNAL_STORAGE";

    public static final String TIME_PASSWORD_OK = "TIME_PASSWORD_OK";
    public static final String TIME_TWO_STEP = "TIME_TWO_STEP";
    public static final String TIME_C2DM = "TIME_C2DM";
    public static final String TIME_LOCATION_AWARE = "TIME_LOCATION_AWARE";

    public static final int BUILD_VERSION_CODES_10 = 29;

    public static final int NOTIFY_ANDROID_6 = 6;
    public static final String NOTIFICATION_POPUP_ID = "NOTIFICATION_POPUP_ID";


    public static final String SENT_UUID_SERIAL_NUMBER = "SENT_UUID_SERIAL_NUMBER";

    public static final String LAST_EVENT_GEO = "LAST_EVENT_GEO";

    public static final String MESSAGE_ID="messageID";

    public static final String JOB_ID="device_job_id";

    public static final String UNLOCK_PASS="unlock_pass";

    public static final String NOTIFICATION_ANDROID_7="notify_android_7";

    public static final String JOB_ID_LOCK="job_id_lock";

    public static final String COUNTER_OFF="counter_off";
    public static final String PIN_ACTIVATED="pin_activated";

    public static final String SSID="SSID";
    public static final String IMEI="IMEI";
    public static final String MODEL="MODEL";
    public static final String PUBLIC_IP="PUBLIC_IP";
    public static final String LOCATION_LAT="LOCATION_LAT";
    public static final String LOCATION_LNG="LOCATION_LNG";
    public static final String AWARE_LAT="AWARE_LAT";
    public static final String AWARE_LNG="AWARE_LNG";
    public static final String AWARE_ACC="AWARE_ACC";
    public static final String AWARE_DATE="AWARE_DATE";

    public static final String AUTO_CONNECT="auto_connect";
    public static final String AWARE="aware";

    public static final String TIME_BLOCK_APP_UNINSTALL= "TIME_BLOCK_APP_UNINSTALL";

    public static final String REPORT_NUMBER= "REPORT_NUMBER";
    public static final String PREFS_BIOMETRIC="PREFS_BIOMETRIC";
    public static final String INSTALLATION_STATUS="INSTALLATION_STATUS";

    public static final String LOCATION_INFO="LOCATION_INFO";
    public static final String CAPS_LOCK_ON="CAPS_LOCK_ON";
    public static final String OVER_LOCK="OVER_LOCK";


    private boolean securityPrivilegesAlreadyPrompted;

    private Context ctx;


    public static String postUrl = null;


    private long installationDate;
    private boolean run;
    private boolean registerC2dm=false;

    private boolean scheduled;
    private int minuteScheduled;
    private int timeoutReport;
    private boolean runOnce;

    private boolean disablePowerOptions;
    private String version;

    private PreyConfig(Context ctx) {
        this.ctx = ctx;
        try {
            this.scheduled = getBoolean(PreyConfig.SCHEDULED, FileConfigReader.getInstance(ctx).isScheduled());
        } catch ( Exception e) {
        } catch ( NoClassDefFoundError e) {}
        try {
            this.timeoutReport = getInt(PreyConfig.TIMEOUT_REPORT, FileConfigReader.getInstance(ctx).getTimeoutReport());
        } catch ( Exception e){
        } catch ( NoClassDefFoundError e) {}
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            this.disablePowerOptions = settings.getBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, false);
        } catch ( Exception e){
        } catch ( NoClassDefFoundError e) {}
        try {
            version =getString(PreyConfig.PREY_VERSION, getInfoPreyVersion(ctx));
        }catch ( Exception e){
        }

    }

    public static synchronized PreyConfig getPreyConfig(Context ctx) {
        if (cachedInstance==null){
            synchronized(PreyConfig.class) {
                if (cachedInstance == null)
                    cachedInstance = new PreyConfig(ctx);
            }
        }
        return cachedInstance;
    }

    public static void deleteCacheInstance(Context ctx) {
        cachedInstance = null;
        PreferenceManager.getDefaultSharedPreferences(ctx).
                edit().clear().apply();
    }

    public Context getContext(){
        return ctx;
    }

    private void saveString(String key, String value){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(key, value);
            editor.commit();
        }catch(Exception e){}
    }

    private String getString(String key,String defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return  settings.getString(key, defaultValue);
    }

    private void saveInt(String key, int value){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(key, value);
            editor.commit();
        }catch(Exception e){}
    }

    private int getInt(String key,int defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getInt(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(key, value);
            editor.commit();
        }catch(Exception e){}
    }

    public boolean getBoolean(String key,boolean defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getBoolean(key, defaultValue);
    }

    private void saveLong(String key, long value){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(key, value);
            editor.commit();
        }catch(Exception e){}
    }

    private void saveFloat(String key, float value){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat(key, value);
            editor.commit();
        }catch(Exception e){}
    }

    private void removeKey(String key){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(key);
            editor.commit();
        }catch(Exception e){
            PreyLogger.e("removeKey:"+e.getMessage(),e);
        }
    }

    private long getLong(String key, long defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getLong(key, defaultValue);
    }

    private float getFloat(String key, float defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getFloat(key, defaultValue);
    }

    public void setProtectAccount(boolean protectAccount) {
        this.saveBoolean(PreyConfig.PROTECT_ACCOUNT, protectAccount);
    }

    public boolean getProtectAccount() {
        return getBoolean(PreyConfig.PROTECT_ACCOUNT, false);
    }

    public void setProtectPrivileges(boolean protectPrivileges) {
        this.saveBoolean(PreyConfig.PROTECT_PRIVILEGES, protectPrivileges);
    }

    public boolean getProtectPrivileges() {
        return getBoolean(PreyConfig.PROTECT_PRIVILEGES, false);
    }

    public void setProtectTour(boolean protectTour) {
        this.saveBoolean(PreyConfig.PROTECT_TOUR, protectTour);
    }

    public boolean getProtectReady() {
        return getBoolean(PreyConfig.PROTECT_READY, false);
    }

    public void setProtectReady(boolean protectReady) {
        this.saveBoolean(PreyConfig.PROTECT_READY, protectReady);
    }

    public boolean getProtectTour() {
        return getBoolean(PreyConfig.PROTECT_TOUR, false);
    }



    public void setCanAccessFineLocation(boolean canAccessFineLocation) {
        this.saveBoolean(PreyConfig.CAN_ACCESS_FINE_LOCATION, canAccessFineLocation);
    }


    public String getError() {
        return getString(PreyConfig.ERROR, null);
    }

    public void setError(String error) {
        this.saveString(PreyConfig.ERROR, error);
    }


    public boolean canAccessFineLocation() {
        return getBoolean(PreyConfig.CAN_ACCESS_FINE_LOCATION, false);
    }

    public void setCanAccessCoarseLocation(boolean canAccessCoarseLocation) {
        this.saveBoolean(PreyConfig.CAN_ACCESS_COARSE_LOCATION, canAccessCoarseLocation);
    }

    public boolean canAccessCoarseLocation() {
        return getBoolean(PreyConfig.CAN_ACCESS_COARSE_LOCATION, false);
    }

    public void setCanAccessCamara(boolean canAccessCamara) {
        this.saveBoolean(PreyConfig.CAN_ACCESS_CAMARA, canAccessCamara);
    }

    public boolean canAccessCamara() {
        return getBoolean(PreyConfig.CAN_ACCESS_CAMARA, false);
    }

    public void setCanAccessReadPhoneState(boolean canAccessReadPhoneState) {
        this.saveBoolean(PreyConfig.CAN_ACCESS_READ_PHONE_STATE, canAccessReadPhoneState);
    }

    public boolean canAccessReadPhoneState() {
        return getBoolean(PreyConfig.CAN_ACCESS_READ_PHONE_STATE, false);
    }

    public void setCanAccessExternalStorage(boolean canAccessExternalStorage) {
        this.saveBoolean(PreyConfig.CAN_ACCESS_EXTERNAL_STORAGE, canAccessExternalStorage);
    }

    public boolean canAccessExternalStorage() {
        return getBoolean(PreyConfig.CAN_ACCESS_EXTERNAL_STORAGE, false);
    }

    public String getApiKey(){
        return getString(PreyConfig.API_KEY, null);
    }

    public void setApiKey(String apikey){
        this.saveString(PreyConfig.API_KEY, apikey);
    }

    public String getDeviceId(){
        return getString(PreyConfig.DEVICE_ID, null);
    }

    public void setDeviceId(String deviceId){
        this.saveString(PreyConfig.DEVICE_ID, deviceId);
    }

    public String getUnlockPass(){
        return getString(PreyConfig.UNLOCK_PASS, null);
    }

    public void setUnlockPass(String unlockPass){
        this.saveString(PreyConfig.UNLOCK_PASS, unlockPass);
    }

    public String getNotificationAndroid7(){
        return getString(PreyConfig.NOTIFICATION_ANDROID_7, null);
    }

    public void setNotificationAndroid7(String notificationAndroid7){
        this.saveString(PreyConfig.NOTIFICATION_ANDROID_7, notificationAndroid7);
    }

    public void setSecurityPrivilegesAlreadyPrompted(boolean securityPrivilegesAlreadyPrompted) {
        this.securityPrivilegesAlreadyPrompted = securityPrivilegesAlreadyPrompted;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PreyConfig.PREFS_SECURITY_PROMPT_SHOWN, securityPrivilegesAlreadyPrompted);
        editor.commit();
    }

    public String getInfoPreyVersion(Context ctx) {
        String versionName=VERSION_PREY_DEFAULT;
        try{
            PackageInfo pinfo =ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            versionName = pinfo.versionName;
        }catch(Exception e){
        }
        return versionName;
    }

    public boolean isFroyoOrAbove() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public boolean isGingerbreadOrAbove() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public boolean isIceCreamSandwichOrAbove() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public boolean isEclairOrAbove(){
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR;
    }

    public boolean isCupcakeOrAbove() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE;
    }

    public boolean isMarshmallowOrAbove() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public boolean isNougatOrAbove() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public boolean isAndroid10OrAbove() {
        return android.os.Build.VERSION.SDK_INT >= PreyConfig.BUILD_VERSION_CODES_10;
    }

    public String getLastEvent() {
        return getString(PreyConfig.LAST_EVENT, null);
    }

    public void setLastEvent(String lastEvent) {
        saveString(PreyConfig.LAST_EVENT, lastEvent);
    }

    public long getLowBatteryDate() {
        return getLong(PreyConfig.LOW_BATTERY_DATE, 0);
    }

    public void setLowBatteryDate(long lowBatteryDate) {
        saveLong(PreyConfig.LOW_BATTERY_DATE, lowBatteryDate);
    }

    public String getPreviousSsid() {
        return getString(PreyConfig.PREVIOUS_SSID, null);
    }

    public void setPreviousSsid(String previousSsid) {
        this.saveString(PreyConfig.PREVIOUS_SSID, previousSsid);
    }

    public void removeDeviceId(){
        this.removeKey(PreyConfig.DEVICE_ID);
    }
    public void removeEmail(){
        this.removeKey(PreyConfig.EMAIL);
    }
    public void removeApiKey(){
        this.removeKey(PreyConfig.API_KEY);
    }

    public boolean isThisDeviceAlreadyRegisteredWithPrey(boolean notifyUser) {
        String deviceId = getString(PreyConfig.DEVICE_ID, null);
        boolean isVerified = (deviceId != null && !"".equals(deviceId));
        return isVerified;
    }

    public boolean isThisDeviceAlreadyRegisteredWithPrey() {
        String deviceID=getDeviceId();
        return deviceID!=null&&!"".equals(deviceID);
    }

    public String getSimSerialNumber(){
        return getString(PreyConfig.SIM_SERIAL_NUMBER,null);
    }

    public void setSimSerialNumber(String simSerialNumber) {
        saveString(PreyConfig.SIM_SERIAL_NUMBER,simSerialNumber);
    }

    public boolean isConnectionExists() {
        boolean isConnectionExists = false;
        // There is wifi connexion?
        if (PreyConnectivityManager.getInstance(ctx).isWifiConnected()) {
            isConnectionExists = true;
        }
        // if there is no connexion wifi, verify mobile connection?
        if (!isConnectionExists && PreyConnectivityManager.getInstance(ctx).isMobileConnected()) {
            isConnectionExists = true;
        }
        return isConnectionExists;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }


    public void registerC2dm() {
       // synchronized(PreyConfig.class) {
            String deviceId = PreyConfig.getPreyConfig(ctx).getDeviceId();
            boolean isTimeC2dm=PreyConfig.getPreyConfig(ctx).isTimeC2dm();
            PreyLogger.d("registerC2dm deviceId:"+deviceId+" isTimeC2dm:"+isTimeC2dm);
            if (deviceId != null && !"".equals(deviceId)) {
                 if (!isTimeC2dm) {
                    String token = null;
                    try {
                        token = FirebaseInstanceId.getInstance().getToken();
                        PreyLogger.d("registerC2dm token2:" + token);
                        sendToken(ctx, token);
                    } catch (Exception e) {
                        PreyLogger.e("registerC2dm error:" + e.getMessage(), e);
                        try {
                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String token = instanceIdResult.getToken();
                                    sendToken(ctx, token);
                                }
                            });
                        } catch (Exception ex) {
                            PreyLogger.e("registerC2dm error2:" + ex.getMessage(), ex);
                        }
                    }
                 }
            }
     //   }
    }

    public static void sendToken(Context ctx,String token) {
        PreyLogger.d("registerC2dm send token:"+token);
        if(token!=null && !"null".equals(token) && !"".equals(token) && UtilConnection.isInternetAvailable(ctx)) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                String registration = FileConfigReader.getInstance(ctx).getGcmIdPrefix() + token;
                PreyLogger.d("registerC2dm registration:" + registration);
                PreyHttpResponse response = PreyWebServices.getInstance().setPushRegistrationId(ctx, registration);
                PreyConfig.getPreyConfig(ctx).setNotificationId(registration);
                if (response != null) {
                    PreyLogger.d("registerC2dm response:" + response.toString());
                }
                PreyConfig.getPreyConfig(ctx).setRegisterC2dm(true);
                PreyConfig.getPreyConfig(ctx).setTimeC2dm();
            } catch (Exception e) {
                PreyLogger.e("registerC2dm error:"+e.getMessage(),e);
            }
        }
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {

            return false;
        }
        return true;
    }

    public void unregisterC2dm(boolean updatePrey){
        try {
            if (updatePrey)
                PreyWebServices.getInstance().setPushRegistrationId(ctx, "");
            Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
            unregIntent.putExtra("app", PendingIntent.getBroadcast(this.ctx, 0, new Intent(), 0));
            this.ctx.startService(unregIntent);
        }catch(Exception e){

        }

    }

    public boolean isDisablePowerOptions(){
        return getDisablePowerOptions();
    }

    public boolean getDisablePowerOptions() {
        return getBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, false);
    }

    public void setDisablePowerOptions(boolean disablePowerOptions) {
        saveBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, disablePowerOptions);
    }

    public boolean isRunBackground(){
        return getRunBackground();
    }

    public boolean getRunBackground() {
        return getBoolean(PreyConfig.PREFS_RUN_BACKGROUND, false);
    }

    public void setRunBackground(boolean disablePowerOptions) {
        saveBoolean(PreyConfig.PREFS_RUN_BACKGROUND, disablePowerOptions);
        saveBoolean(PreyConfig.PREFS_BACKGROUND, disablePowerOptions);
    }

    public void setNextAlert(boolean nextAlert){
        saveBoolean(PreyConfig.NEXT_ALERT, nextAlert);
    }

    public boolean isNextAlert() {
        return getBoolean(PreyConfig.NEXT_ALERT, false);
    }

    public void setCamouflageSet(boolean camouflageSet){
        this.saveBoolean(PreyConfig.IS_CAMOUFLAGE_SET, camouflageSet);
    }

    public boolean isCamouflageSet() {
        return getBoolean(PreyConfig.IS_CAMOUFLAGE_SET, false);
    }

    public String getPreyPanelUrl() {
        String panel = FileConfigReader.getInstance(this.ctx).getPreyPanel();
        String url= HTTP.concat(panel).concat(".").concat(getPreyDomain()).concat("/").concat(getPreyCampaign());
        return url;
    }

    public String getPreyPanelJwt() {
        String panel = FileConfigReader.getInstance(this.ctx).getPreyPanel();
        String url= HTTP.concat(panel).concat(".").concat(getPreyDomain()).concat("/").concat(getPreyJwt());
        return url;
    }

    public String getPreyUrl() {
        String subdomain = FileConfigReader.getInstance(this.ctx).getPreySubdomain();
        return HTTP.concat(subdomain).concat(".").concat(getPreyDomain()).concat("/");

    }

    public String getPreyDomain() {
        return FileConfigReader.getInstance(this.ctx).getPreyDomain();
    }

    public String getPreyCampaign() {
        return FileConfigReader.getInstance(this.ctx).getPreyCampaign();
    }

    public String getPreyJwt() {
        return FileConfigReader.getInstance(this.ctx).getPreyJwt();
    }

    public String getPreyGooglePlay() {
        return FileConfigReader.getInstance(this.ctx).getPreyGooglePlay();
    }
    public String getPreyUninstallUrl() {
        return FileConfigReader.getInstance(this.ctx).getPreyUninstall();
    }
    public String getPreyUninstallEsUrl() {
        return FileConfigReader.getInstance(this.ctx).getPreyUninstallEs();
    }
    public String getApiKeyBatch() {
        return FileConfigReader.getInstance(this.ctx).getApiKeyBatch();
    }

    public String getEmailBatch() {
        return FileConfigReader.getInstance(this.ctx).getEmailBatch();
    }

    public int getFlagFeedback() {
        return getInt(PreyConfig.FLAG_FEEDBACK, 0);
    }

    public void setFlagFeedback(int flagFeedback) {
        saveInt(PreyConfig.FLAG_FEEDBACK, flagFeedback);
    }

    public boolean showFeedback() {
        return FeedbackActivity.showFeedback(getLong(PreyConfig.INSTALLATION_DATE, 0), getFlagFeedback());
    }

    public void setAccountVerified(boolean accountVerified) {
        saveBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, accountVerified);
    }

    public boolean isAccountVerified() {
        return getBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, false);
    }

    public String getEmail() {
        return getString(PreyConfig.EMAIL, "");
    }

    public void setEmail(String email) {
        saveString(PreyConfig.EMAIL, email);
    }

    public boolean getTwoStep() {
        return getBoolean(PreyConfig.TWO_STEP, false);
    }

    public void setTwoStep(boolean twoStep) {
        saveBoolean(PreyConfig.TWO_STEP, twoStep);
    }


    public void setAccountVerified() {
        saveBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, true);
    }


    public boolean isSendData(){
        return getBoolean(PreyConfig.SEND_DATA, false);
    }

    public void setSendData(boolean sendData) {
        saveBoolean(PreyConfig.SEND_DATA, sendData);
    }

    public void saveAccount(PreyAccountData accountData) {
        saveBoolean(PreyConfig.ACCOUNT,true);
        saveString(PreyConfig.DEVICE_ID, accountData.getDeviceId());
        saveString(PreyConfig.API_KEY, accountData.getApiKey());
        saveString(PreyConfig.EMAIL, accountData.getEmail());
    }

    public boolean isAccount() {
        return getBoolean(PreyConfig.ACCOUNT, false);
    }


    public boolean isMissing() {
        return getBoolean(PreyConfig.PREFS_IS_MISSING, false);
    }

    public void setMissing(boolean missing) {
        saveBoolean(PreyConfig.PREFS_IS_MISSING, missing);
    }

    public boolean isRunOnce() {
        return runOnce;
    }

    public void setRunOnce(boolean runOnce) {
        this.runOnce = runOnce;
    }

    public void wipeData() {

        long installationDate=getLong(PreyConfig.INSTALLATION_DATE, 0);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();

        if(installationDate>0) {
            saveLong(PreyConfig.INSTALLATION_DATE, installationDate);
        }
    }




    public void setRevokedPassword(boolean isRevokedPassword, String revokedPassword) {

        saveBoolean(PreyConfig.IS_REVOKED_PASSWORD, isRevokedPassword);
        saveString(PreyConfig.REVOKED_PASSWORD, revokedPassword);

    }


    public void setNotificationId(String notificationId) {
        saveString(PreyConfig.NOTIFICATION_ID, notificationId);
    }

    public String getNotificationId(){

        return getString(PreyConfig.NOTIFICATION_ID, "");
    }


    public void setRegisterC2dm(boolean registerC2dm){
        this.registerC2dm=registerC2dm;
    }
    public boolean isRegisterC2dm(){
        return registerC2dm;
    }


    public String getIntervalReport(){
        return getString(PreyConfig.INTERVAL_REPORT,"");
    }

    public void setIntervalReport(String intervalReport) {

        this.saveString(PreyConfig.INTERVAL_REPORT, intervalReport);
    }

    public String getExcludeReport(){
        return getString(PreyConfig.EXCLUDE_REPORT,"");
    }

    public void setExcludeReport(String excludeReport) {
        saveString(PreyConfig.EXCLUDE_REPORT, excludeReport);
    }

    public void setLastReportStartDate(long lastReportStartDate){
        saveLong(PreyConfig.LAST_REPORT_START_DATE, lastReportStartDate);
    }

    public long getLastReportStartDate(){
        return getLong(PreyConfig.LAST_REPORT_START_DATE, 0);
    }


    public void setTimeSecureLock(long timeSecureLock){
        saveLong(PreyConfig.TIME_SECURE_LOCK, timeSecureLock);
    }

    public long getTimeSecureLock(){
        return getLong(PreyConfig.TIME_SECURE_LOCK, 0);
    }


    public String getIntervalAware(){
        return getString(PreyConfig.INTERVAL_AWARE,"");
    }

    public void setIntervalAware(String intervalAware) {

        this.saveString(PreyConfig.INTERVAL_AWARE, intervalAware);
    }


    public void setInstallationDate(long installationDate){
        saveLong(PreyConfig.INSTALLATION_DATE, installationDate);
    }

    public long getInstallationDate(){
        return getLong(PreyConfig.INSTALLATION_DATE, 0);
    }

    public void setLocationLowBatteryDate(long locationLowBatteryDate){
        saveLong(PreyConfig.LOCATION_LOW_BATTERY_DATE, locationLowBatteryDate);
    }

    public long getLocationLowBatteryDate(){
        return getLong(PreyConfig.LOCATION_LOW_BATTERY_DATE, 0);
    }

    public void setScheduled(boolean scheduled){
        this.scheduled=scheduled;
        saveBoolean(PreyConfig.SCHEDULED, scheduled);
    }

    public boolean isScheduled(){
        return false;
    }


    public boolean isOverOtherApps(){
        return FileConfigReader.getInstance(ctx).isOverOtherApps();
    }

    public boolean isAskForNameBatch(){
        return FileConfigReader.getInstance(ctx).isAskForNameBatch();
    }

    public void setMinuteScheduled(int minuteScheduled){
        saveInt(PreyConfig.MINUTE_SCHEDULED, minuteScheduled);
    }

    public int getMinuteScheduled(){
        return getInt(PreyConfig.MINUTE_SCHEDULED, 0);
    }

    public int getTimeoutReport(){
        return timeoutReport;
    }

    public String getSessionId(){
        return getString(PreyConfig.SESSION_ID, "");
    }

    public void setSessionId(String sessionId) {
        saveString(PreyConfig.SESSION_ID, sessionId);
    }

    public void setPinNumber(String pin){
        saveString(PreyConfig.PIN_NUMBER2, pin);
    }

    public String getPinNumber() {
        String pin=getString(PreyConfig.PIN_NUMBER2, "");
        if(pin.length()>4){
            pin=pin.substring(0,4);
        }
        return pin;
    }

    public void setSmsCommand(boolean smsCommand) {
        saveBoolean(PreyConfig.SMS_COMMAND, smsCommand);
    }

    public boolean isSmsCommand(){
        return getBoolean(PreyConfig.SMS_COMMAND, false);
    }

    public void setLocationLowBattery(boolean locationLowBattery) {
        saveBoolean(PreyConfig.PREFERENCE_LOCATION_LOW_BATTERY, locationLowBattery);
    }

    public boolean isLocationLowBattery() {
        return getBoolean(PreyConfig.PREFERENCE_LOCATION_LOW_BATTERY, false);
    }

    public int getGeofenceMaximumAccuracy(){
        return FileConfigReader.getInstance(this.ctx).getGeofenceMaximumAccuracy();
    }

    public int getDistanceLocation(){
        return FileConfigReader.getInstance(this.ctx).getDistanceLocation();
    }

    public int getDistanceAware(){
        return FileConfigReader.getInstance(this.ctx).getDistanceAware();
    }

    public boolean isSentUuidSerialNumber() {
        return getBoolean(PreyConfig.SENT_UUID_SERIAL_NUMBER, false);
    }

    public void setSentUuidSerialNumber(boolean sentUuidSerialNumber){
        saveBoolean(PreyConfig.SENT_UUID_SERIAL_NUMBER, sentUuidSerialNumber);
    }

    public String getTokenJwt(){
        return getString(PreyConfig.TOKEN_JWT, "");
    }

    public void setTokenJwt(String tokenJwt) {
        saveString(PreyConfig.TOKEN_JWT, tokenJwt);
    }

    public void setTimePasswordOk(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,3);
        saveLong(TIME_PASSWORD_OK,cal.getTimeInMillis());
    }

    public boolean isTimePasswordOk(){
        long timePasswordOk=getLong(TIME_PASSWORD_OK,0);
        long timeNow=new Date().getTime();
        if (timeNow<timePasswordOk){
            return true;
        } else {
            return false;
        }
    }

    public String getLastEventGeo(){
        return getString(PreyConfig.LAST_EVENT_GEO, "");
    }

    public void setLastEventGeo(String lastEventGeo) {
        PreyLogger.d("lastEventGeo["+lastEventGeo+"]");
        saveString(PreyConfig.LAST_EVENT_GEO, lastEventGeo);
    }

    public boolean isChromebook() {
        return ctx.getPackageManager().hasSystemFeature("org.chromium.arc.device_management");
    }

    public String getJobIdLock(){
        return getString(PreyConfig.JOB_ID_LOCK, "");
    }

    public void setJobIdLock(String jobIdLock) {
        saveString(PreyConfig.JOB_ID_LOCK, jobIdLock);
    }

    public int getCounterOff(){
        return getInt(PreyConfig.COUNTER_OFF,0);
    }

    public void setCounterOff(int counter){
        saveInt(PreyConfig.COUNTER_OFF,counter);
    }

    boolean openSecureService=false;
    public void setOpenSecureService(boolean openSecureService){
        this.openSecureService=openSecureService;
    }

    public boolean isOpenSecureService(){
        return openSecureService;
    }

    public void setLastTimeSecureLock(long lastTimeSecureLock){
        saveLong(PreyConfig.LAST_TIME_SECURE_LOCK, lastTimeSecureLock);
    }

    public long getLastTimeSecureLock(){
        return getLong(PreyConfig.LAST_TIME_SECURE_LOCK, 0);
    }

    public static SimpleDateFormat FORMAT_SDF_AWARE=new SimpleDateFormat("yyyy-MM-dd");

    public void setLocation(PreyLocation location){
        if(location!=null) {
            saveFloat(PreyConfig.LOCATION_LAT, (float) location.getLat().floatValue());
            saveFloat(PreyConfig.LOCATION_LNG, (float) location.getLng().floatValue());
        }
    }
    public PreyLocation getLocation(){
        PreyLocation location=new PreyLocation();
        float lat= getFloat(PreyConfig.LOCATION_LAT ,0);
        float lng= getFloat(PreyConfig.LOCATION_LNG,0 );
        location.setLat(lat);
        location.setLng(lng);
        return location;
    }

    public void setLocationAware(PreyLocation location){
        if(location!=null) {
            saveFloat(PreyConfig.AWARE_LAT, location.getLat().floatValue());
            saveFloat(PreyConfig.AWARE_LNG, location.getLng().floatValue());
            saveFloat(PreyConfig.AWARE_ACC, location.getAccuracy());
        }
    }

    public void removeLocationAware(){
        saveFloat(PreyConfig.AWARE_LAT, 0);
        saveFloat(PreyConfig.AWARE_LNG, 0);
        saveFloat(PreyConfig.AWARE_ACC, 0);
        saveString(PreyConfig.AWARE_DATE, "");
    }

    public String getAwareDate(){
        return getString(PreyConfig.AWARE_DATE, "");
    }

    public void setAwareDate(String awareDate){
        PreyLogger.d("AWARE setAwareDate ["+awareDate+"]");
        saveString(PreyConfig.AWARE_DATE, awareDate);
    }


    public PreyLocation getLocationAware(){
        try{
            float lat=getFloat(PreyConfig.AWARE_LAT,0);
            float lng=getFloat(PreyConfig.AWARE_LNG,0);
            float acc=getFloat(PreyConfig.AWARE_ACC,0);
            if(lat==0||lng==0){
                return null;
            }
            PreyLocation location= new PreyLocation();
            location.setLat(lat);
            location.setLng(lng);
            location.setAccuracy(acc);
            return location;
        }catch(Exception e){
            return null;
        }
    }

    public void setPinActivated(String number_activated) {
        this.saveString(PreyConfig.PIN_NUMBER_ACTIVATE, number_activated);
    }

    public String getPinActivated() {
        return getString(PreyConfig.PIN_NUMBER_ACTIVATE, "");
    }

    public void setAware(boolean aware) {
        this.saveBoolean(PreyConfig.AWARE, aware);
    }

    public boolean getAware() {
        return getBoolean(PreyConfig.AWARE, false);
    }

    public void setAutoConnect(boolean auto_connect) {
        this.saveBoolean(PreyConfig.AUTO_CONNECT, auto_connect);
    }

    public boolean getAutoConnect() {
        return getBoolean(PreyConfig.AUTO_CONNECT, false);
    }

    public String getPreyVersion(){
        return version;
    }

    public void setPreyVersion(String version) {
        this.version=version;
        this.saveString(PreyConfig.PREY_VERSION, version);
    }

    public boolean isBlockAppUninstall(){
        return getBlockAppUninstall();
    }

    public boolean getBlockAppUninstall() {
        return getBoolean(PREFS_BLOCK_APP_UNINSTALL, false);
    }

    public void setBlockAppUninstall(boolean blockAppUninstall) {
        saveBoolean(PREFS_BLOCK_APP_UNINSTALL, blockAppUninstall);
    }

    public void setTimeBlockAppUninstall(long timeBlockAppUninstall){
        saveLong(TIME_BLOCK_APP_UNINSTALL, timeBlockAppUninstall);
    }

    public long getTimeBlockAppUninstall(){
        return getLong(TIME_BLOCK_APP_UNINSTALL, 0);
    }

    public int getReportNumber(){
        return getInt(REPORT_NUMBER, 0);
    }
    public void setReportNumber(int number_report){
        saveInt(REPORT_NUMBER, number_report);
    }

    public boolean getPrefsBiometric() {
        return getBoolean(PREFS_BIOMETRIC, false);
    }
    public void setPrefsBiometric(boolean prefsBiometric) {
        saveBoolean(PREFS_BIOMETRIC, prefsBiometric);
    }

    public String getPublicIp() {
        return getString(PUBLIC_IP, "");
    }
    public void setPublicIp(String publicIp) {
        saveString(PUBLIC_IP, publicIp);
    }

    public String getSsid() {
        return getString(SSID, "");
    }
    public void setSsid(String ssid) {
        saveString(SSID, ssid);
    }

    public String getImei() {
        return getString(IMEI, "");
    }
    public void setImei(String imei) {
        saveString(IMEI, imei);
    }

    public String getModel() {
        return getString(MODEL, "");
    }
    public void setModel(String model) {
        saveString(MODEL, model);
    }



    public void setTimeTwoStep(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,2);
        saveLong(TIME_TWO_STEP,cal.getTimeInMillis());
    }

    public boolean isTimeTwoStep(){
        long timePasswordOk=getLong(TIME_TWO_STEP,0);
        long timeNow=new Date().getTime();
        if (timeNow<timePasswordOk){
            return true;
        } else {
            return false;
        }
    }

    public void setTimeC2dm(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,5);
        saveLong(TIME_C2DM,cal.getTimeInMillis());
    }

    public boolean isTimeC2dm(){
        long timeC2dm=getLong(TIME_C2DM,0);
        long timeNow=new Date().getTime();
        if (timeNow<timeC2dm){
            return true;
        } else {
            return false;
        }
    }

    public void initTimeC2dm(){
        saveLong(TIME_C2DM,0);
    }

    public void setTimeLocationAware(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,1);
        saveLong(TIME_LOCATION_AWARE,cal.getTimeInMillis());
    }

    public boolean isTimeLocationAware(){
        long timeLocationAware=getLong(TIME_LOCATION_AWARE,0);
        long timeNow=new Date().getTime();
        if (timeNow<timeLocationAware){
            return true;
        } else {
            return false;
        }
    }

    public void setNoficationPopupId(int noficationPopupId){
        saveInt(NOTIFICATION_POPUP_ID,noficationPopupId);
    }

    public int getNoficationPopupId() {
        return getInt(NOTIFICATION_POPUP_ID, 0);
    }

    public String getInstallationStatus(){
        return getString(PreyConfig.INSTALLATION_STATUS, "");
    }

    public void setInstallationStatus(String installationStatus){
        saveString(PreyConfig.INSTALLATION_STATUS, installationStatus);
    }

    public String getLocationInfo(){
        return getString(PreyConfig.LOCATION_INFO, "");
    }

    public void setLocationInfo(String locationInfo){
        saveString(PreyConfig.LOCATION_INFO, locationInfo);
    }

    public View viewLock=null;
    public View viewSecure=null;

    public boolean getCapsLockOn(){
        return getBoolean(PreyConfig.CAPS_LOCK_ON, false);
    }

    public void setCapsLockOn(boolean capsLockOn){
        saveBoolean(PreyConfig.CAPS_LOCK_ON, capsLockOn);
    }

    public boolean getOverLock(){
        return getBoolean(PreyConfig.OVER_LOCK, false);
    }

    public void setOverLock(boolean overLock){
        saveBoolean(PreyConfig.OVER_LOCK, overLock);
    }
}