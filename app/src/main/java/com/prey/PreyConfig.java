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
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.prey.activities.FeedbackActivity;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.PreyWebServices;
import com.prey.services.PreyDisablePowerOptionsService;
import com.prey.services.PreyRegistrationIntentService;

import java.util.Date;
import java.util.Locale;

public class PreyConfig {

    //Set false in production
    public static final boolean LOG_DEBUG_ENABLED = false;

    private static PreyConfig cachedInstance = null;

    public static final String TAG = "PREY";

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

    public static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

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
    public static final String IS_LOCK_SET="IS_LOCK_SET";
    public static final String NEXT_ALERT="NEXT_ALERT";
    public static final String IS_CAMOUFLAGE_SET="IS_CAMOUFLAGE_SET";
    public static final String PREFS_RINGTONE="PREFS_RINGTONE";

    public static final String LAST_EVENT="LAST_EVENT";
    public static final String LOW_BATTERY_DATE="LOW_BATTERY_DATE";
    public static final String PREVIOUS_SSID="PREVIOUS_SSID";


    public static final String FLAG_FEEDBACK="FLAG_FEEDBACK";
    public static final String INSTALLATION_DATE="INSTALLATION_DATE";

    public static final String PREFS_ACCOUNT_VERIFIED="PREFS_ACCOUNT_VERIFIED";
    public static final String EMAIL="EMAIL";
    public static final String PREFS_SCHEDULED="PREFS_SCHEDULED";

    public static final String SEND_DATA="SEND_DATA";
    public static final String SCHEDULED="SCHEDULED";
    public static final String MINUTE_SCHEDULED="MINUTE_SCHEDULED";

    public static final String IS_REVOKED_PASSWORD="IS_REVOKED_PASSWORD";
    public static final String REVOKED_PASSWORD="REVOKED_PASSWORD";
    public static final String NOTIFICATION_ID="NOTIFICATION_ID";
    public static final String INTERVAL_REPORT="INTERVAL_REPORT";
    public static final String EXCLUDE_REPORT="EXCLUDE_REPORT";
    public static final String LAST_REPORT_START_DATE="LAST_REPORT_START_DATE";
    public static final String TIMEOUT_REPORT="TIMEOUT_REPORT";

    public static final String SIGNAL_FLARE_DATE="SIGNAL_FLARE_DATE";
    public static final String SESSION_ID="SIGNAL_FLARE_DATE";

    public static final String PIN_NUMBER="PIN_NUMBER";
    public static final String SMS_COMMAND="SMS_COMMAND";

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

    public static final String PREFERENCE_PREY_VERSION="PREFERENCE_PREY_VERSION";
    public static final String API_KEY="API_KEY";
    public static final String DEVICE_ID = "DEVICE_ID";

    public static final String SIM_SERIAL_NUMBER = "SIM_SERIAL_NUMBER";
    public static final String VERSION_PREY_DEFAULT="1.4.7";

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


    private PreyConfig(Context ctx) {
        this.ctx = ctx;




        this.scheduled=getBoolean(PreyConfig.SCHEDULED, FileConfigReader.getInstance(ctx).isScheduled());
        this.minuteScheduled=getInt(PreyConfig.MINUTE_SCHEDULED, FileConfigReader.getInstance(ctx).getMinuteScheduled());
        this.timeoutReport=getInt(PreyConfig.TIMEOUT_REPORT, FileConfigReader.getInstance(ctx).getTimeoutReport());
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.disablePowerOptions = settings.getBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, false);

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

    private static void deleteCacheInstance() {
        cachedInstance = null;
    }

    private void saveString(String key, String value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String getString(String key,String defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getString(key, defaultValue);
    }

    private void saveInt(String key, int value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private int getInt(String key,int defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getInt(key, defaultValue);
    }

    private void saveBoolean(String key, boolean value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private boolean getBoolean(String key,boolean defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getBoolean(key, defaultValue);
    }

    private void saveLong(String key, long value){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private long getLong(String key, long defaultValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getLong(key, defaultValue);
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


    public String getApiKey(){
        return getString(PreyConfig.API_KEY, null);
    }

    public void setApiKey(String apikey){
        this.saveString(PreyConfig.API_KEY,apikey);
    }

    public String getDeviceId(){
        return getString(PreyConfig.DEVICE_ID,null);
    }

    public void setDeviceId(String deviceId){
        this.saveString(PreyConfig.DEVICE_ID, deviceId);
    }

    public void setSecurityPrivilegesAlreadyPrompted(boolean securityPrivilegesAlreadyPrompted) {
        this.securityPrivilegesAlreadyPrompted = securityPrivilegesAlreadyPrompted;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PreyConfig.PREFS_SECURITY_PROMPT_SHOWN, securityPrivilegesAlreadyPrompted);
        editor.commit();
    }


    public String getPreyVersion() {
        String versionName=VERSION_PREY_DEFAULT;
        try{
            PackageInfo pinfo =ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            versionName = pinfo.versionName;
        }catch(Exception e){
        }
        return versionName;
    }

    public boolean isFroyoOrAbove() {
        return android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.FROYO;
    }

    public boolean isGingerbreadOrAbove() {
        return android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.GINGERBREAD;
    }

    public boolean isIceCreamSandwichOrAbove() {
        return android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public boolean isEclairOrAbove(){
        return android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.ECLAIR;
    }

    public boolean isCupcakeOrAbove() {
        return android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.CUPCAKE;
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


    public boolean isThisDeviceAlreadyRegisteredWithPrey(boolean notifyUser) {
        String deviceId = getString(PreyConfig.DEVICE_ID, null);
        PreyLogger.d("deviceId:"+deviceId);
        boolean isVerified = (deviceId != null && !"".equals(deviceId));
        return isVerified;
    }

    public boolean isThisDeviceAlreadyRegisteredWithPrey() {
        String deviceID=getDeviceId();
        return deviceID!=null&&!"".equals(deviceID);
    }

    public boolean isSimChanged() {
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String simSerial=telephonyManager.getSimSerialNumber();
        PreyLogger.i("simSerial:" + simSerial + " actual:" + getSimSerialNumber());
        if (getSimSerialNumber()==null||"".equals(getSimSerialNumber())){
            if(simSerial!=null&&!"".equals(simSerial)){
                this.setSimSerialNumber(simSerial);
            }
            return false;
        }
        if(simSerial!=null&&!"".equals(simSerial)&&!simSerial.equals(this.getSimSerialNumber())){
            return true;
        }
        return false;
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


    public void registerC2dm(){
        boolean error=false;

            if (PreyEmail.getEmail(this.ctx) != null) {
                String deviceId = PreyConfig.getPreyConfig(ctx).getDeviceId();
                if (deviceId != null && !"".equals(deviceId)) {
                    try {

                        PreyLogger.d("______________________");
                        PreyLogger.d("___ registerC2dm _____");


                        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
                        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this.ctx, 0, new Intent(), 0)); // boilerplate
                        String gcmId = FileConfigReader.getInstance(this.ctx).getGcmId();

                        registrationIntent.putExtra("sender", gcmId);
                        this.ctx.startService(registrationIntent);
                        PreyLogger.d("______________________");

                    } catch (Exception e) {
                        error = true;

                    }

                    if (error) {
                        try {
                            if (PreyEmail.getEmail(this.ctx) != null) {
                                PreyLogger.d("______________________");
                                PreyLogger.d("___ registerC2dm  2_____");


                                PreyLogger.i("starservice RegistrationIntentService");
                                Intent intent = new Intent(ctx, PreyRegistrationIntentService.class);
                                ctx.startService(intent);


                                PreyLogger.d("______________________");
                            }
                        } catch (Exception e) {
                            PreyLogger.e("Error :" + e.getMessage(), e);

                        }
                    }
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
        PreyLogger.i(url);
        return url;
    }

    private static final String HTTP="https://";

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

    public String getPreyUninstallUrl() {
        String url = FileConfigReader.getInstance(this.ctx).getPreyUninstall();
        PreyLogger.i(url);
        return url;
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

    public void setEmail(String email){
        saveString(PreyConfig.EMAIL, email);
    }

    public void setAccountVerified() {
        saveBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, true);
    }


    public boolean isSendData(){
        return getBoolean(PreyConfig.SEND_DATA,false);
    }

    public void setSendData(boolean sendData) {
         saveBoolean(PreyConfig.SEND_DATA, sendData);
    }

    public void saveAccount(PreyAccountData accountData) {

        this.saveSimInformation();

        saveString(PreyConfig.DEVICE_ID, accountData.getDeviceId());
        saveString(PreyConfig.API_KEY, accountData.getApiKey());
        saveString(PreyConfig.EMAIL, accountData.getEmail());


    }

    public void saveSimInformation() {
        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String simSerial=telephonyManager.getSimSerialNumber();
        if (simSerial !=null){
            this.setSimSerialNumber(simSerial);
        }
        this.saveString(PreyConfig.PREFS_SIM_SERIAL_NUMBER, this.getSimSerialNumber());
        PreyLogger.d("SIM Serial number stored: " + this.getSimSerialNumber());
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
        return getString(PreyConfig.INTERVAL_REPORT,"");
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

    public void setInstallationDate(long installationDate){
        saveLong(PreyConfig.INSTALLATION_DATE, installationDate);
    }

    public long getInstallationDate(){
        return getLong(PreyConfig.INSTALLATION_DATE, 0);
    }

    public void setSignalFlareDate(long signalFlareDate){
        saveLong(PreyConfig.SIGNAL_FLARE_DATE, signalFlareDate);
    }

    public long getSignalFlareDate(){
        return getLong(PreyConfig.SIGNAL_FLARE_DATE, 0);
    }

    public void setScheduled(boolean scheduled){
        this.scheduled=scheduled;
        saveBoolean(PreyConfig.SCHEDULED, scheduled);
    }

    public boolean isScheduled(){
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                if (PreyEmail.getEmail(ctx)!=null)
                    return false;
            }
        }catch(Exception e){
            return false;
        }
        return scheduled;
    }

    public void setMinuteScheduled(int minuteScheduled){
        this.minuteScheduled=minuteScheduled;
        saveInt(PreyConfig.MINUTE_SCHEDULED, minuteScheduled);
    }

    public int getMinuteScheduled(){
        return minuteScheduled;
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

    public void setPinNumber(int pin){
        saveInt(PreyConfig.PIN_NUMBER, pin);
    }

    public int getPinNumber(){
        return getInt(PreyConfig.PIN_NUMBER,-1);
    }

    public void setSmsCommand(boolean smsCommand){
        saveBoolean(PreyConfig.SMS_COMMAND, smsCommand);
    }

    public boolean isSmsCommand(){
        return getBoolean(PreyConfig.SMS_COMMAND, false);
    }

    public String getPreferencePreyVersion(){
        return getString(PreyConfig.PREFERENCE_PREY_VERSION, "");
    }

    public void setPreferencePreyVersion(String preferencePreyVersion) {
        saveString(PreyConfig.PREFERENCE_PREY_VERSION, preferencePreyVersion);
    }

}
