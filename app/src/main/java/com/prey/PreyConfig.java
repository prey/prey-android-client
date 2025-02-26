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

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prey.actions.aware.AwareController;
import com.prey.actions.location.PreyLocation;
import com.prey.activities.FeedbackActivity;
import com.prey.json.actions.Location;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.UtilConnection;
import com.prey.preferences.RunBackgroundCheckBoxPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PreyConfig {

    //Set false in production
    public static final boolean LOG_DEBUG_ENABLED = false;
    private static PreyConfig cachedInstance = null;
    public static final String TAG = "PREY";
    private static final String HTTP = "https://";
    public static final String VERSION_PREY_DEFAULT = "2.4.9";
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
    public static final long FASTEST_INTERVAL = 40 * MILLISECONDS_PER_SECOND;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10 * MILLISECONDS_PER_SECOND;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final String PROTECT_ACCOUNT = "PROTECT_ACCOUNT";
    public static final String PROTECT_PRIVILEGES = "PROTECT_PRIVILEGES";
    public static final String PROTECT_TOUR = "PROTECT_TOUR";
    public static final String PROTECT_READY = "PROTECT_READY";
    public static final String PREFS_SIM_SERIAL_NUMBER = "PREFS_SIM_SERIAL_NUMBER";
    public static final String PREFS_SECURITY_PROMPT_SHOWN = "PREFS_SECURITY_PROMPT_SHOWN";
    public static final String PREFS_IS_MISSING = "PREFS_IS_MISSING";
    public static final String PREFS_DISABLE_POWER_OPTIONS = "PREFS_DISABLE_POWER_OPTIONS";
    public static final String PREFS_BLOCK_APP_UNINSTALL = "PREFS_BLOCK_APP_UNINSTALL";
    public static final String PREFS_RUN_BACKGROUND = "PREFS_RUN_BACKGROUND";
    public static final String PREFS_USE_BIOMETRIC = "PREFS_USE_BIOMETRIC";
    public static final String PREFS_BACKGROUND = "PREFS_BACKGROUND";
    public static final String IS_LOCK_SET = "IS_LOCK_SET";
    public static final String NEXT_ALERT = "NEXT_ALERT";
    public static final String IS_CAMOUFLAGE_SET = "IS_CAMOUFLAGE_SET";
    public static final String PREFS_RINGTONE = "PREFS_RINGTONE";
    public static final String LAST_EVENT = "LAST_EVENT";
    public static final String LOW_BATTERY_DATE = "LOW_BATTERY_DATE";
    public static final String PREVIOUS_SSID = "PREVIOUS_SSID";
    public static final String ERROR = "ERROR";
    public static final String FLAG_FEEDBACK = "FLAG_FEEDBACK";
    public static final String INSTALLATION_DATE = "INSTALLATION_DATE";
    public static final String PREFS_ACCOUNT_VERIFIED = "PREFS_ACCOUNT_VERIFIED";
    public static final String EMAIL = "EMAIL";
    public static final String TWO_STEP = "TWO_STEP";
    public static final String PRO_ACCOUNT = "PRO_ACCOUNT";
    public static final String SEND_DATA = "SEND_DATA";
    public static final String SCHEDULED = "SCHEDULED";
    public static final String MINUTE_SCHEDULED = "MINUTE_SCHEDULED2";
    public static final String IS_REVOKED_PASSWORD = "IS_REVOKED_PASSWORD";
    public static final String REVOKED_PASSWORD = "REVOKED_PASSWORD";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String INTERVAL_REPORT = "INTERVAL_REPORT";
    public static final String EXCLUDE_REPORT = "EXCLUDE_REPORT";
    public static final String LAST_REPORT_START_DATE = "LAST_REPORT_START_DATE";
    public static final String TIMEOUT_REPORT = "TIMEOUT_REPORT";
    public static final String INTERVAL_AWARE = "INTERVAL_AWARE";
    public static final String TIME_SECURE_LOCK = "TIME_SECURE_LOCK";
    public static final String LAST_TIME_SECURE_LOCK = "LAST_TIME_SECURE_LOCK";
    public static final String LOCATION_LOW_BATTERY_DATE = "LOCATION_LOW_BATTERY_DATE";
    public static final String SESSION_ID = "SESSION_ID";
    public static final String PIN_NUMBER2 = "PIN_NUMBER2";
    public static final String SMS_COMMAND = "SMS_COMMAND";
    public static final String PREFERENCE_LOCATION_LOW_BATTERY = "PREFERENCE_LOCATION_LOW_BATTERY";
    public static final String TOKEN_JWT = "TOKEN_JWT";
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
    public static final String PREY_VERSION = "PREY_VERSION";
    public static final String API_KEY = "API_KEY";
    public static final String DEVICE_ID = "DEVICE_ID";
    public static final String ACCOUNT = "ACCOUNT";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String SIM_SERIAL_NUMBER = "SIM_SERIAL_NUMBER";
    public static final String CAN_ACCESS_FINE_LOCATION = "CAN_ACCESS_FINE_LOCATION";
    public static final String CAN_ACCESS_COARSE_LOCATION = "CAN_ACCESS_COARSE_LOCATION";
    public static final String CAN_ACCESS_CAMARA = "CAN_ACCESS_CAMARA";
    public static final String CAN_ACCESS_READ_PHONE_STATE = "CAN_ACCESS_READ_PHONE_STATE";
    public static final String CAN_ACCESS_EXTERNAL_STORAGE = "CAN_ACCESS_EXTERNAL_STORAGE";
    public static final String TIME_PASSWORD_OK = "TIME_PASSWORD_OK";
    public static final String TIME_TWO_STEP = "TIME_TWO_STEP";
    public static final String TIME_C2DM = "TIME_C2DM";
    public static final String TIME_LOCATION_AWARE = "TIME_LOCATION_AWARE";
    public static final int BUILD_VERSION_CODES_10 = 29;
    public static final int BUILD_VERSION_CODES_11 = 30;
    public static final int NOTIFY_ANDROID_6 = 6;
    public static final String NOTIFICATION_POPUP_ID = "NOTIFICATION_POPUP_ID";
    public static final String SENT_UUID_SERIAL_NUMBER = "SENT_UUID_SERIAL_NUMBER";
    public static final String LAST_EVENT_GEO = "LAST_EVENT_GEO";
    public static final String MESSAGE_ID = "messageID";
    public static final String JOB_ID = "device_job_id";
    public static final String UNLOCK_PASS = "unlock_pass";
    public static final String LOCK_MESSAGE = "lock_message";
    public static final String NOTIFICATION_ANDROID_7 = "notify_android_7";
    public static final String JOB_ID_LOCK = "job_id_lock";
    public static final String COUNTER_OFF = "counter_off";
    public static final String SSID = "SSID";
    public static final String IMEI = "IMEI";
    public static final String MODEL = "MODEL";
    public static final String PUBLIC_IP = "PUBLIC_IP";
    public static final String LOCATION_LAT = "LOCATION_LAT";
    public static final String LOCATION_LNG = "LOCATION_LNG";
    public static final String LOCATION_ACCURACY = "LOCATION_ACCURACY";
    public static final String AWARE_LAT = "AWARE_LAT";
    public static final String AWARE_LNG = "AWARE_LNG";
    public static final String AWARE_ACC = "AWARE_ACC";
    public static final String AWARE_DATE = "AWARE_DATE";
    public static final String AUTO_CONNECT = "auto_connect";
    public static final String AWARE = "aware";
    public static final String TIME_BLOCK_APP_UNINSTALL = "TIME_BLOCK_APP_UNINSTALL";
    public static final String REPORT_NUMBER = "REPORT_NUMBER";
    public static final String PREFS_BIOMETRIC = "PREFS_BIOMETRIC";
    public static final String INSTALLATION_STATUS = "INSTALLATION_STATUS";
    public static final String LOCATION_INFO = "LOCATION_INFO";
    public static final String CAPS_LOCK_ON = "CAPS_LOCK_ON";
    public static final String VERIFICATE_BIOMETRIC = "VERIFICATE_BIOMETRIC";
    public static final String TYPE_BIOMETRIC = "TYPE_BIOMETRIC";
    public static final String OVER_LOCK = "OVER_LOCK";
    public static final String FIRST = "FIRST";
    public static final String PIN_NUMBER_ACTIVATE = "PIN_NUMBER_ACTIVATE";
    public static final String INPUT_WEBVIEW = "INPUT_WEBVIEW";
    public static final String PAGE = "PAGE";
    public static final String PERMISSION_LOCATION = "PERMISSION_LOCATION";
    public static final String HELP_FILE = "HELP_FILE";
    public static final String CONTACT_FORM_FOR_FREE = "CONTACT_FORM_FOR_FREE";
    public static final String VIEW_SECURE = "VIEW_SECURE";
    public static final String HELP_DIRECTORY = "preyHelp";
    public static final String TIME_NEXT_ACCESSIBILITY = "TIME_NEXT_ACCESSIBILITY";
    public static final String ACCESSIBILITY_DENIED = "ACCESSIBILITY_DENIED";
    public static final String TIME_NEXT_ALLFILES = "TIME_NEXT_ALLFILES";
    public static final String ALLFILES_DENIED = "ALLFILES_DENIED";
    public static final String TIME_NEXT_LOCATIONBG = "TIME_NEXT_LOCATIONBG";
    public static final String LOCATIONBG_DENIED = "LOCATIONBG_DENIED";
    public static final String MSP_ACCOUNT = "MSP_ACCOUNT";
    public static final String START = "START";

    public static final String VOLUME= "VOLUME";
    public static final String DENY_NOTIFICATION= "DENY_NOTIFICATION";
    public static final String TIME_NEXT_PING = "TIME_NEXT_PING";
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
            PreyLogger.e("Error:"+e.getMessage(),e);
        } catch ( NoClassDefFoundError e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        try {
            this.timeoutReport = getInt(PreyConfig.TIMEOUT_REPORT, FileConfigReader.getInstance(ctx).getTimeoutReport());
        } catch ( Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        } catch ( NoClassDefFoundError e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            this.disablePowerOptions = settings.getBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, false);
        } catch ( Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        } catch ( NoClassDefFoundError e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        try {
            version =getString(PreyConfig.PREY_VERSION, getInfoPreyVersion(ctx));
        }catch ( Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
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
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
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
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
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
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
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
        }catch(Exception e){PreyLogger.e("Error:"+e.getMessage(),e);}
    }

    private void saveFloat(String key, float value){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat(key, value);
            editor.commit();
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
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

    public String getDeviceName(){
        return getString(PreyConfig.DEVICE_NAME, "");
    }

    public void setDeviceName(String deviceName){
        this.saveString(PreyConfig.DEVICE_NAME, deviceName);
    }

    public String getUnlockPass(){
        return getString(PreyConfig.UNLOCK_PASS, null);
    }

    public void setUnlockPass(String unlockPass){
        this.saveString(PreyConfig.UNLOCK_PASS, unlockPass);
    }

    public String getLockMessage(){
        return getString(PreyConfig.LOCK_MESSAGE, null);
    }

    public void setLockMessage(String unlockPass){
        this.saveString(PreyConfig.LOCK_MESSAGE, unlockPass);
    }

    public void deleteUnlockPass(){
        this.removeKey(PreyConfig.UNLOCK_PASS);
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
            PreyLogger.e("Error:"+e.getMessage(),e);
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
        String deviceID = getDeviceId();
        return deviceID != null && !"".equals(deviceID);
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
                            try {
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (!task.isSuccessful()) {
                                                    PreyLogger.e(String.format("registerC2dm error:%s", task.getException().getMessage()), task.getException());
                                                }
                                                String token = task.getResult();
                                                PreyLogger.d(String.format("registerC2dm token:%s", token));
                                                sendToken(ctx, token);
                                            }
                                        });
                            } catch (Exception exception) {
                                PreyLogger.e(String.format("registerC2dm error:%s", exception.getMessage()), exception);
                            }
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
            unregIntent.putExtra("app", PendingIntent.getBroadcast(this.ctx, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE));
            this.ctx.startService(unregIntent);
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
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

    public boolean getUseBiometric() {
        return getBoolean(PreyConfig.PREFS_USE_BIOMETRIC, false);
    }

    public void setUseBiometric(boolean useBiometric) {
        saveBoolean(PreyConfig.PREFS_USE_BIOMETRIC, useBiometric);
    }

    public void setRunBackground(boolean disablePowerOptions) {
        saveBoolean(PreyConfig.PREFS_RUN_BACKGROUND, disablePowerOptions);
        saveBoolean(PreyConfig.PREFS_BACKGROUND, disablePowerOptions);
    }

    public void setLock(boolean locked) {
        saveBoolean(PreyConfig.IS_LOCK_SET, locked);
    }

    public boolean isLockSet() {
        return getBoolean(PreyConfig.IS_LOCK_SET, false);
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
        return PreyBatch.getInstance(this.ctx).getApiKeyBatch();
    }

    public String getEmailBatch() {
        return PreyBatch.getInstance(this.ctx).getEmailBatch();
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

    public boolean getProAccount() {
        return getBoolean(PreyConfig.PRO_ACCOUNT, false);
    }

    public void setProAccount(boolean proAccount) {
        saveBoolean(PreyConfig.PRO_ACCOUNT, proAccount);
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

    public boolean isAskForNameBatch(){
        return PreyBatch.getInstance(ctx).isAskForNameBatch();
    }

    public void setScheduled(boolean scheduled){
        this.scheduled=scheduled;
        saveBoolean(PreyConfig.SCHEDULED, scheduled);
    }

    public boolean isScheduled(){
        return false;
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

    public void removeTimePasswordOk(){
        Calendar cal= Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,-3);
        saveLong(TIME_PASSWORD_OK,cal.getTimeInMillis());
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
            saveFloat(PreyConfig.LOCATION_ACCURACY, (float) location.getAccuracy());
        }
    }
    public PreyLocation getLocation(){
        PreyLocation location=new PreyLocation();
        float lat = getFloat(PreyConfig.LOCATION_LAT, 0);
        float lng = getFloat(PreyConfig.LOCATION_LNG, 0);
        float accuracy = getFloat(PreyConfig.LOCATION_ACCURACY, 0);
        location.setLat(lat);
        location.setLng(lng);
        location.setAccuracy(accuracy);
        return location;
    }

    public void setLocationAware(PreyLocation location){
        if(location!=null) {
            saveString(PreyConfig.AWARE_LAT, location.getLat().toString());
            saveString(PreyConfig.AWARE_LNG, location.getLng().toString());
            saveFloat(PreyConfig.AWARE_ACC, location.getAccuracy());
        }
    }

    public void removeLocationAware(){
        saveString(PreyConfig.AWARE_LAT, "");
        saveString(PreyConfig.AWARE_LNG, "");
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

    /**
     * Retrieves the location aware settings.
     *
     * @return A PreyLocation object containing the location aware settings, or null if the settings are not available.
     */
    public PreyLocation getLocationAware() {
        try {
            // Initialize latitude and longitude variables
            String lat = "";
            String lng = "";
            // Attempt to retrieve the latitude and longitude values from storage
            //The data saving is changed to string because decimals are lost
            try {
                lat = getString(PreyConfig.AWARE_LAT, "");
                lng = getString(PreyConfig.AWARE_LNG, "");
            } catch (Exception e) {
                PreyLogger.e(String.format("Error getLocationAware:%s", e.getMessage()), e);
            }
            // Retrieve the accuracy value from storage
            float acc = getFloat(PreyConfig.AWARE_ACC, 0f);
            // Check if the latitude or longitude values are empty or null
            if (lat == null || "".equals(lat) || lng == null || "".equals(lng)) {
                // If either value is empty or null, return null
                return null;
            }
            // Create a new PreyLocation object
            PreyLocation location = new PreyLocation();
            location.setLat(Double.parseDouble(lat));
            location.setLng(Double.parseDouble(lng));
            location.setAccuracy(acc);
            // Return the PreyLocation object
            return location;
        } catch (Exception e) {
            PreyLogger.e(String.format("Error getLocationAware:%s", e.getMessage()), e);
            return null;
        }
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

    public boolean getVerificateBiometric(){
        boolean verificateBiometric = getBoolean(PreyConfig.VERIFICATE_BIOMETRIC, false);
        if(verificateBiometric){
            setVerificateBiometric(false);
        }
        return verificateBiometric;
    }
    public void setVerificateBiometric(boolean verificateBiometric){
        saveBoolean(PreyConfig.VERIFICATE_BIOMETRIC, verificateBiometric);
    }

    public String getTypeBiometric() {
        return getString(PreyConfig.TYPE_BIOMETRIC, "");
    }
    public void setTypeBiometric(String typeBiometric){
        saveString(PreyConfig.TYPE_BIOMETRIC, typeBiometric);
    }

    public boolean getOverLock(){
        return getBoolean(PreyConfig.OVER_LOCK, false);
    }

    public void setOverLock(boolean overLock){
        saveBoolean(PreyConfig.OVER_LOCK, overLock);
    }

    public boolean isFirst(){
        return getBoolean(PreyConfig.FIRST, true);
    }

    public void setFirst(boolean first){
        saveBoolean(PreyConfig.FIRST, first);
    }

    public void setPinActivated(String number_activated) {
        this.saveString(PreyConfig.PIN_NUMBER_ACTIVATE, number_activated);
    }

    public String getPinActivated() {
        return getString(PreyConfig.PIN_NUMBER_ACTIVATE, "");
    }

    public String getInputWebview() {
        String inputWebview=getString(INPUT_WEBVIEW, "");
        PreyLogger.d("getInputWebview:"+inputWebview);
        return inputWebview;
    }
    public void setInputWebview(String inputWebview) {
        saveString(INPUT_WEBVIEW, inputWebview);
    }
    public String getPage() {
        String page=getString(PAGE, "");
        PreyLogger.d("page:"+page);
        return page;
    }
    public void setPage(String page) {
        saveString(PAGE, page);
    }

    public void setPermissionLocation(boolean permission_location) {
        this.saveBoolean(PreyConfig.PERMISSION_LOCATION, permission_location);
    }

    public boolean getPermissionLocation() {
        return getBoolean(PreyConfig.PERMISSION_LOCATION, true);
    }

    public String getHelpFile() {
        return getString(HELP_FILE, "");
    }

    public void setFileHelp(String fileHelp) {
        saveString(HELP_FILE, fileHelp);
    }

    public boolean getHelpFormForFree() {
        return getBoolean(PreyConfig.CONTACT_FORM_FOR_FREE, false);
    }

    public void setContactFormForFree(boolean contactFree) {
        saveBoolean(PreyConfig.CONTACT_FORM_FOR_FREE, contactFree);
    }

    public boolean getViewSecure(){
        return getBoolean(PreyConfig.VIEW_SECURE, false);
    }

    public void setViewSecure(boolean viewSecure){
        saveBoolean(PreyConfig.VIEW_SECURE, viewSecure);
    }

    /**
     * Method add a minute to request accessibility permission
     */
    public void setTimeNextAccessibility() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 1);
        saveLong(TIME_NEXT_ACCESSIBILITY, cal.getTimeInMillis());
    }

    /**
     * Method that returns if it should request accessibility permission
     *
     * @return if you must ask
     */
    public boolean isTimeNextAccessibility() {
        long timeLocationAware = getLong(TIME_NEXT_ACCESSIBILITY, 0);
        long timeNow = new Date().getTime();
        return timeNow < timeLocationAware;
    }

    /**
     * Method to deny accessibility permission
     *
     * @param denied
     */
    public void setAccessibilityDenied(boolean denied) {
        saveBoolean(PreyConfig.ACCESSIBILITY_DENIED, denied);
    }

    /**
     * Method that gets whether to deny accessibility permission
     *
     * @return if you should deny
     */
    public boolean getAccessibilityDenied() {
        return getBoolean(PreyConfig.ACCESSIBILITY_DENIED, false);
    }

    /**
     * Method add a minute to request storage permission
     */
    public void setTimeNextAllFiles() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 1);
        saveLong(TIME_NEXT_ALLFILES, cal.getTimeInMillis());
    }

    /**
     * Method that returns if it should request storage permission
     *
     * @return if you must ask
     */
    public boolean isTimeNextAllFiles() {
        long timeLocationAllfiles = getLong(TIME_NEXT_ALLFILES, 0);
        long timeNow = new Date().getTime();
        return timeNow < timeLocationAllfiles;
    }

    /**
     * Method to deny storage permission
     *
     * @param denied
     */
    public void setAllFilesDenied(boolean denied) {
        saveBoolean(PreyConfig.ALLFILES_DENIED, denied);
    }

    /**
     * Method that gets whether to deny storage permission
     *
     * @return if you should deny
     */
    public boolean getAllFilesDenied() {
        return getBoolean(PreyConfig.ALLFILES_DENIED, false);
    }

    /**
     * Method add a minute to request location background permission
     */
    public void setTimeNextLocationBg() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 1);
        saveLong(TIME_NEXT_LOCATIONBG, cal.getTimeInMillis());
    }

    /**
     * Method that returns if it should request location background permission
     *
     * @return if you must ask
     */
    public boolean isTimeNextLocationBg() {
        long timeLocationBg = getLong(TIME_NEXT_LOCATIONBG, 0);
        long timeNow = new Date().getTime();
        return timeNow < timeLocationBg;
    }

    /**
     * Method to deny location background permission
     *
     * @param denied
     */
    public void setLocationBgDenied(boolean denied) {
        saveBoolean(PreyConfig.LOCATIONBG_DENIED, denied);
    }

    /**
     * Method that gets whether to deny location background permission
     *
     * @return if you should deny
     */
    public boolean getLocationBgDenied() {
        return getBoolean(PreyConfig.LOCATIONBG_DENIED, false);
    }

    public boolean getMspAccount() {
        return getBoolean(PreyConfig.MSP_ACCOUNT, false);
    }

    public void setMspAccount(boolean mspAccount) {
        saveBoolean(PreyConfig.MSP_ACCOUNT, mspAccount);
    }

    public boolean getStart() {
        return getBoolean(PreyConfig.START, true);
    }

    public void setStart(boolean start) {
        saveBoolean(PreyConfig.START, start);
    }

    public void setTimeNextPing() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 1);
        saveLong(TIME_NEXT_PING, cal.getTimeInMillis());
    }

    /**
     * Method that returns if I should verify the internet with ping
     *
     * @return if check
     */
    public boolean isTimeNextPing() {
        long timePing = getLong(TIME_NEXT_PING, 0);
        long timeNow = new Date().getTime();
        return timeNow < timePing;
    }

    public void setVolume(int volume) {
        saveInt(PreyConfig.VOLUME, volume);
    }

    /**
     * Method that returns the volume before the report
     *
     * @return volume
     */
    public int getVolume() {
        return getInt(PreyConfig.VOLUME, 0);
    }

    public void setDenyNotification(boolean denyNotification) {
        this.saveBoolean(PreyConfig.DENY_NOTIFICATION, denyNotification);
    }

    /**
     * Method to deny notification permission
     *
     * @return denied
     */
    public boolean getDenyNotification() {
        return getBoolean(PreyConfig.DENY_NOTIFICATION, false);
    }

    public static final String DAILY_LOCATION = "DAILY_LOCATION";

    public String getDailyLocation(){
        return getString(PreyConfig.DAILY_LOCATION, "");
    }

    public void setDailyLocation(String dailyLocation){
        PreyLogger.d(String.format("DAILY setDailyLocation [%s]", dailyLocation));
        saveString(PreyConfig.DAILY_LOCATION, dailyLocation);
    }

    public static final String MINUTES_TO_QUERY_SERVER = "MINUTES_TO_QUERY_SERVER";

    public int getMinutesToQueryServer() {
        return getInt(PreyConfig.MINUTES_TO_QUERY_SERVER, 15);
    }

    public void setMinutesToQueryServer(int minutesToQueryServer) {
        PreyLogger.d(String.format("setMinutesToQueryServer [%s]", minutesToQueryServer));
        saveInt(PreyConfig.MINUTES_TO_QUERY_SERVER, minutesToQueryServer);
    }

    /**
     * Key for storing the aware time in the configuration.
     */
    public static final String AWARE_TIME = "AWARE_TIME";

    /**
     * Sets the aware time to 10 minutes in the future.
     *
     * This method updates the aware time stored in the configuration.
     */
    public void setAwareTime() {
        //the date is saved 10 minutes in the future
        Calendar cal=Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE ,10);
        long dateTimeLong=cal.getTimeInMillis();
        PreyLogger.d(String.format("AWARE WORK AwareTime [%s]", dateTimeLong));
        saveLong(PreyConfig.AWARE_TIME, dateTimeLong);
    }

    /**
     * Checks if it's time for the next aware event.
     *
     * This method compares the current time with the saved aware time.
     * It is used to not request the location for at least 10 minutes
     *
     * @return true if it's time for the next aware event, false otherwise
     */
    public boolean isTimeNextAware() {
        //validates if the saved date is old
        long awareTime = getLong(AWARE_TIME, 0);
        if (awareTime == 0)
            return true;
        long timeNow = new Date().getTime();
        PreyLogger.d(String.format("AWARE WORK AwareTime difference [%s] current[%s] > save[%s] ", (timeNow - awareTime), timeNow, awareTime));
        return timeNow > awareTime;
    }

    /**
     * Registers a new device with the given API key.
     *
     * @param apiKey The API key to register the device with.
     * @throws Exception If there is an error during the registration process.
     */
    public void registerNewDeviceWithApiKey(String apiKey) throws Exception {
        // Check if the device is already registered with Prey
        if (!isThisDeviceAlreadyRegisteredWithPrey()) {
            // Get the device type and name
            String deviceType = PreyUtils.getDeviceType(ctx);
            String nameDevice = PreyUtils.getNameDevice(ctx);
            PreyLogger.d(String.format("apikey:%s type:%s nameDevice:%s", apiKey, deviceType, nameDevice));
            // Register the device with the API key, device type, and name
            PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, apiKey, deviceType, nameDevice);
            if (accountData != null) {
                PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                // Register C2DM
                PreyConfig.getPreyConfig(ctx).registerC2dm();
                // Get the email associated with the account
                String email = PreyWebServices.getInstance().getEmail(ctx);
                PreyConfig.getPreyConfig(ctx).setEmail(email);
                PreyConfig.getPreyConfig(ctx).setRunBackground(true);
                RunBackgroundCheckBoxPreference.notifyReady(ctx);
                PreyConfig.getPreyConfig(ctx).setInstallationStatus("");
                // Run the Prey app
                new PreyApp().run(ctx);
                // Start a new thread to initialize PreyStatus and Location
                new Thread() {
                    public void run() {
                        try {
                            PreyStatus.getInstance().initConfig(ctx);
                            AwareController.getInstance().init(ctx);
                            new Location().get(ctx, null, null);
                        } catch (Exception e) {
                            // Log any errors that occur during initialization
                            PreyLogger.e("Error:" + e.getMessage(), e);
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * Key for storing the organization ID in the configuration.
     */
    public static final String ORGANIZATION_ID = "ORGANIZATION_ID";

    /**
     * Retrieves the organization ID from the configuration.
     *
     * @return The organization ID, or an empty string if not set.
     */
    public String getOrganizationId() {
        // Retrieve the organization ID from the configuration, defaulting to an empty string if not set
        return getString(ORGANIZATION_ID, "");
    }

    /**
     * Sets the organization ID in the configuration.
     *
     * @param organizationId The organization ID to set.
     */
    public void setOrganizationId(String organizationId) {
        // Save the organization ID to the configuration
        saveString(ORGANIZATION_ID, organizationId);
    }

}
