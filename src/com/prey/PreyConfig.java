/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.prey.actions.LockAction;
import com.prey.actions.PreyAction;
import com.prey.activities.WelcomeActivity;
import com.prey.net.PreyWebServices;

public class PreyConfig {
	
	//Set false in production
	public static final boolean LOG_DEBUG_ENABLED = false;
	
	// Set to 1000 * 60 in production.
	public static final long DELAY_MULTIPLIER = 1000 * 60; 
	
	// the minimum time interval for GPS notifications, in milliseconds (default 60000).
	public static final long LOCATION_PROVIDERS_MIN_REFRESH_INTERVAL = 60000;
	
	// the minimum distance interval for GPS notifications, in meters (default 20)
	public static final float LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE = 20;
	
	// max "age" in ms of last location (default 120000).
	public static final float LAST_LOCATION_MAX_AGE = 120000;

	//Amount of millisecond the app can be suspended before ask for the password.
	public static final long PASSWORD_PROMPT_DELAY = 5000;


	public static final String PREFS_NAME = "PREY_PREFS";
	public static final String PREFS_URL_KEY = "URL";
	public static final String PREFS_DEVICE_ID = "DEVICE_ID";
	public static final String PREFS_API_KEY = "API_KEY";
	public static final String PREFS_LOGIN = "LOGIN";
	public static final String PREFS_EMAIL = "EMAIL";
	//public static final String PREFS_PASSWORD = "PASSWORD";
	public static final String PREFS_PREY_VERSION = "PREY_VERSION";
	public static final String PREFS_IS_MISSING = "IS_MISSING";
	public static final String PREFS_SMS_RUN = "PREFS_SMS_RUN";
	public static final String PREFS_SMS_STOP = "PREFS_SMS_STOP";
	public static final String PREFS_DESTINATION_SMS = "PREFS_DESTINATION_SMS";
	public static final String PREFS_SHOW_NOTIFICATION = "PREFS_SHOW_NOTIFICATION";
	public static final String PREFS_CHECK_SIM_CHANGE = "PREFS_CHECK_SIM_CHANGE";
	public static final String PREFS_RINGTONE = "PREFS_RINGTONE";
	public static final String PREFS_SIM_SERIAL_NUMBER = "PREFS_SIM_SERIAL_NUMBER";
	public static final String PREFS_ACCOUNT_VERIFIED = "PREFS_ACCOUNT_VERIFIED";
	public static final String PREFS_SECURITY_PROMPT_SHOWN = "PREFS_SECURITY_PROMPT_SHOWN";
	public static final String ACTIVATE_DEVICE_ADMIN = "ACTIVATE_DEVICE_ADMIN";
	public static final String UNLOCK_PASS = "UNLOCK_PASS";
	public static final String IS_LOCK_SET = "IS_LOCK_SET";
	public static final String LAST_LAT = "LAST_LAT";
	public static final String LAST_LON = "LAST_LON";
	public static final String LAST_ACC = "LAST_ACC";
	public static final String LAST_ALT = "LAST_ALT";
	public static final String LAST_LOC_PROVIDER = "LAST_ALT";
	/* ------------- */

	public static final String TAG = "PREY";

	
	private static PreyConfig cachedInstance = null;
	public static String postUrl = null;
	private String deviceID = "";
	private String apiKey = "";
	private boolean missing;
	private String email;
	private String password;
	private String destinationSms;
	private boolean shouldNotify;
	private boolean shouldCheckSimChange;
	private boolean isTheAccountAlreadyVerified;
	private boolean securityPrivilegesAlreadyPrompted;
	private boolean locked;
	private boolean runOnce;
	private boolean isFroyoOrAbove;
	private boolean isCupcake;
	
	private Context ctx;

	private PreyConfig(Context ctx) {
		this.ctx = ctx;
		this.isFroyoOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 8;
		this.isCupcake = Integer.parseInt(Build.VERSION.SDK) == 3;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		settings.registerOnSharedPreferenceChangeListener(listener);
		this.deviceID = settings.getString(PreyConfig.PREFS_DEVICE_ID, "");
		this.apiKey = settings.getString(PreyConfig.PREFS_API_KEY, "");
		//this.password = settings.getString(PreyConfig.PREFS_PASSWORD, password);
		//this.smsToRun = settings.getString(PreyConfig.PREFS_SMS_RUN, "GO PREY");
		//this.smsToStop = settings.getString(PreyConfig.PREFS_SMS_STOP, "STOP PREY");
		this.destinationSms = settings.getString(PreyConfig.PREFS_DESTINATION_SMS, "");
		this.missing = new Boolean(settings.getString(PreyConfig.PREFS_IS_MISSING, "false"));
		this.email = settings.getString(PreyConfig.PREFS_EMAIL, "");
		this.shouldNotify = settings.getBoolean(PreyConfig.PREFS_SHOW_NOTIFICATION, false);
		this.shouldCheckSimChange = settings.getBoolean(PreyConfig.PREFS_CHECK_SIM_CHANGE, true);
		this.isTheAccountAlreadyVerified = settings.getBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, false);
		this.securityPrivilegesAlreadyPrompted = settings.getBoolean(PreyConfig.PREFS_SECURITY_PROMPT_SHOWN, false);
		this.locked = settings.getBoolean(PreyConfig.IS_LOCK_SET, false);
	}
	
	public void saveAccount(PreyAccountData accountData) {
		PreyConfig.deleteCacheInstance();
		this.saveSimInformation();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PreyConfig.PREFS_DEVICE_ID, accountData.getDeviceId());
		editor.putString(PreyConfig.PREFS_API_KEY, accountData.getApiKey());
		editor.putString(PreyConfig.PREFS_LOGIN, accountData.getLogin());
		//editor.putString(PreyConfig.PREFS_PASSWORD, accountData.getPassword());
		editor.putString(PreyConfig.PREFS_EMAIL, accountData.getEmail());
		//editor.putString(PreyConfig.PREFS_PREY_VERSION, accountData.getPreyVersion());
		editor.putString(PreyConfig.PREFS_IS_MISSING, new Boolean(accountData.isMissing()).toString());

		editor.commit();
		
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
	
	SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(PREFS_SHOW_NOTIFICATION))
				shouldNotify = sharedPreferences.getBoolean(PREFS_SHOW_NOTIFICATION, false);
		}
	};


	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
		this.saveString(PreyConfig.PREFS_DEVICE_ID, deviceID);
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		this.saveString(PreyConfig.PREFS_API_KEY, apiKey);
	}

	public boolean isMissing() {
		return missing;
	}

	public void setMissing(boolean missing) {
		this.missing = missing;
		this.saveString(PreyConfig.PREFS_IS_MISSING, new Boolean(missing).toString());
	}
	
	public void setUnlockPass(String unlockPass) {
		this.saveString(PreyConfig.UNLOCK_PASS, unlockPass);
	}
	
	public String getUnlockPass() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.UNLOCK_PASS, "preyrocks");
	}
	
	public void setLock(boolean locked) {
		this.locked = locked;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PreyConfig.IS_LOCK_SET, locked);
		editor.commit();
	}
	
	public void unlockIfLockActionIsntEnabled(ArrayList<PreyAction> actions){
		
		if (isLockSet()){
			boolean disableLock = true;
			for (PreyAction preyAction : actions) {
				if (preyAction.ID.equals(LockAction.DATA_ID)) //Lock action comes in the modules list.
					disableLock = false;
			}
			if (disableLock){
				PreyLogger.d("Lock set before but not now. Removing it!");
				this.setLock(false);
				LockAction lockAction = new LockAction();
				lockAction.killAnyInstanceRunning(ctx);
			}
		}
	}
	
	public boolean isLockSet() {
		return locked;
	}

	/*
	public void setPassword(String password) {
		this.password = password;
		this.saveString(PreyConfig.PREFS_PASSWORD, password);
	}

	public String getPassword() {
		return password;
	}*/

	public String getSmsToStop() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_SMS_STOP, "STOP PREY");
	}

	public String getSmsToRun() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_SMS_RUN, "GO PREY");
	}

	public boolean isShouldNotify() {
		return shouldNotify;
	}

	public boolean isShouldCheckSimChange() {
		return shouldCheckSimChange;
	}

	public String getEmail() {
		return email;
	}

	public void wipeData() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
	}

	public boolean isThisDeviceAlreadyRegisteredWithPrey(boolean notifyUser) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		String deviceId = settings.getString(PreyConfig.PREFS_DEVICE_ID, null);
		
		boolean isVerified = deviceId != null;
		
		if (notifyUser && !isVerified){
			String notificationTitle = ctx.getText(R.string.not_verified_device_title).toString();
			NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_stat_notify_exclamation25, notificationTitle, System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
	
			Intent preyMainActivity = new Intent(ctx, WelcomeActivity.class);
			String notificationToShow = ctx.getText(R.string.not_verified_device_msg).toString();
			PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, preyMainActivity, 0);
			notification.contentIntent = contentIntent;
			notification.setLatestEventInfo(ctx, ctx.getText(R.string.not_verified_device_title), notificationToShow, contentIntent);
	
			nm.notify(R.string.preyForAndroid_name, notification);
		}
			
		return isVerified;
	}

	public void setAccountVerified() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, true);
		editor.commit();
	}

	public boolean isAccountVerified() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, false);
	}

	public void saveSimInformation() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		// String imsi = mTelephonyMgr.getSubscriberId();
		String simSerialNumber = mTelephonyMgr.getSimSerialNumber();
		this.saveString(PreyConfig.PREFS_SIM_SERIAL_NUMBER, simSerialNumber);
		PreyLogger.d("SIM Serial number stored: " + simSerialNumber);
	}

	private boolean isThisTheRegisteredSim() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		// String imsi = mTelephonyMgr.getSubscriberId();
		String simSerialNumber = mTelephonyMgr.getSimSerialNumber();
		if (simSerialNumber == null)
			return true; //Couldn't get the serial number, so can't check.
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		String storedSerialNumber = settings.getString(PreyConfig.PREFS_SIM_SERIAL_NUMBER, "");
		PreyLogger.d("Checking SIM. Current SIM Serial Number: " + storedSerialNumber);
		if (storedSerialNumber.equals(""))
			return true; // true since SIM hasn't been registered.
		
		PreyLogger.d("Checking SIM. Registered SIM Serial Number: " + simSerialNumber);
		return simSerialNumber.equals(storedSerialNumber);
	}

	public boolean isSimChanged() {
		boolean shouldStartOnSimChange = this.isShouldCheckSimChange();
		if (shouldStartOnSimChange)
			return !this.isThisTheRegisteredSim();
		return false;
	}

	public String getDestinationSms() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_DESTINATION_SMS, "");
	}

	public void saveDestinationSms(String destinationSms) {
		this.saveString(PreyConfig.PREFS_DESTINATION_SMS, destinationSms);
	}

	public boolean isSecurityPrivilegesAlreadyPrompted() {
		return securityPrivilegesAlreadyPrompted;
	}

	public void setSecurityPrivilegesAlreadyPrompted(boolean securityPrivilegesAlreadyPrompted) {
		this.securityPrivilegesAlreadyPrompted = securityPrivilegesAlreadyPrompted;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PreyConfig.PREFS_SECURITY_PROMPT_SHOWN, securityPrivilegesAlreadyPrompted);
		editor.commit();
	}
	
	public void registerC2dm(){
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this.ctx, 0, new Intent(), 0)); // boilerplate
		registrationIntent.putExtra("sender", FileConfigReader.getInstance(this.ctx).getc2dmMail());
		this.ctx.startService(registrationIntent);
	}
	
	public void unregisterC2dm(boolean updatePrey){
		if (updatePrey)
			PreyWebServices.getInstance().setPushRegistrationId(ctx, "");
		Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra("app", PendingIntent.getBroadcast(this.ctx, 0, new Intent(), 0));
		this.ctx.startService(unregIntent);
		
	}
	
	private void saveString(String key, String value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public boolean isRunOnce() {
		return runOnce;
	}

	public void setRunOnce(boolean runOnce) {
		this.runOnce = runOnce;
	}

	public boolean isFroyoOrAbove() {
		return isFroyoOrAbove;
	}

	public boolean isCupcake() {
		return isCupcake;
	}

	public String getPreyVersion() {
		return FileConfigReader.getInstance(this.ctx).getPreyVersion();
	}
	
	public String getPreyMinorVersion() {
		return FileConfigReader.getInstance(this.ctx).getPreyMinorVersion();
	}
	
	public String getPreyDomain() {
		return FileConfigReader.getInstance(this.ctx).getPreyDomain();
	}
	
	public String getc2dmAction(){
		return FileConfigReader.getInstance(this.ctx).getc2dmAction();
	}
	public String getc2dmMessageSync(){
		return FileConfigReader.getInstance(this.ctx).getc2dmMessageSync();
	}
	public String getAgreementId(){
		return FileConfigReader.getInstance(this.ctx).getAgreementId();
	}

	public String getPreyUrl() {
		String subdomain = FileConfigReader.getInstance(this.ctx).getPreySubdomain();
		return "http://".concat(subdomain).concat(".").concat(getPreyDomain()).concat("/");
	}

	public boolean askForPassword() {
		boolean ask =  FileConfigReader.getInstance(this.ctx).isAskForPassword();
		PreyLogger.d("Ask for password?"+ask);
		return ask;
	}
	
	public boolean isLogEnabled() {
		return FileConfigReader.getInstance(this.ctx).isLogEnabled();
	}

	

}
