/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;


import com.prey.actions.LockAction;
import com.prey.actions.PreyAction;
import com.prey.activities.FeedbackActivity;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.PreyWebServices;
import com.prey.services.PreyDisablePowerOptionsService;


public class PreyConfig {
	
	//Set false in production
	public static final boolean LOG_DEBUG_ENABLED = false;
	
	// Set to 1000 * 60 in production.
	public static final long DELAY_MULTIPLIER = 1000 * 60; 
	
	// the minimum time interval for GPS notifications, in milliseconds (default 60000).
	public static final long LOCATION_PROVIDERS_MIN_REFRESH_INTERVAL = 10000;
	
	// the minimum distance interval for GPS notifications, in meters (default 20)
	public static final float LOCATION_PROVIDERS_MIN_REFRESH_DISTANCE = 20;
	
	// max "age" in ms of last location (default 120000).
	public static final float LAST_LOCATION_MAX_AGE = 30000;
	
	//Amount of millisecond the app can be suspended before ask for the password. 
	public static final long PASSWORD_PROMPT_DELAY = 5000;
	


	public static final String PREFS_NAME = "PREY_PREFS";
	public static final String PREFS_URL_KEY = "URL";
	public static final String PREFS_DEVICE_ID = "DEVICE_ID";
	public static final String PREFS_API_KEY = "API_KEY";
	public static final String PREFS_LOGIN = "LOGIN";
	public static final String PREFS_EMAIL = "EMAIL";
	public static final String PREFS_PREY_VERSION = "PREY_VERSION";
	public static final String PREFS_IS_MISSING = "IS_MISSING";
	public static final String PREFS_SMS_RUN = "PREFS_SMS_RUN";
	public static final String PREFS_SMS_STOP = "PREFS_SMS_STOP";
	public static final String PREFS_DESTINATION_SMS = "PREFS_DESTINATION_SMS";
	public static final String PREFS_DESTINATION_SMS_NAME = "PREFS_DESTINATION_SMS_NAME";
	public static final String PREFS_CHECK_SIM_CHANGE = "PREFS_CHECK_SIM_CHANGE";
	public static final String PREFS_RINGTONE = "PREFS_RINGTONE";
	public static final String PREFS_SIM_SERIAL_NUMBER = "PREFS_SIM_SERIAL_NUMBER";
	public static final String PREFS_ACCOUNT_VERIFIED = "PREFS_ACCOUNT_VERIFIED";
	public static final String PREFS_SECURITY_PROMPT_SHOWN = "PREFS_SECURITY_PROMPT_SHOWN";
	public static final String PREFS_SCHEDULED = "PREFS_SCHEDULED";
	public static final String ACTIVATE_DEVICE_ADMIN = "ACTIVATE_DEVICE_ADMIN";
 
	public static final String IS_CAMOUFLAGE_SET = "PREFS_CAMOUFLAGE";
	public static final String UNLOCK_PASS = "UNLOCK_PASS";
	public static final String IS_LOCK_SET = "IS_LOCK_SET";
	public static final String LAST_LAT = "LAST_LAT";
	public static final String LAST_LON = "LAST_LON";
	public static final String LAST_ACC = "LAST_ACC";
	public static final String LAST_ALT = "LAST_ALT";
	public static final String LAST_LOC_PROVIDER = "LAST_ALT";
	
	
	public static final String IS_REVOKED_PASSWORD = "IS_REVOKED_PASSWORD";
	public static final String REVOKED_PASSWORD = "REVOKED_PASSWORD";
	
	public static final String INSTALLATION_DATE = "INSTALLATION_DATE"; 
	public static final String FLAG_FEEDBACK = "FLAG_FEEDBACK"; 
	public static final String KEEP_ON = "KEEP_ON";
	public static final String VERSION = "VERSION";
	public static final String VERSION_V1 = "V1";
	public static final String VERSION_V2 = "V2";
	
	public static final String LAST_EVENT="LAST_EVENT";
	public static final String PREVIOUS_SSID="PREVIOUS_SSID";
	public static final String SIM_SERIAL_NUMBER="SIM_SERIAL_NUMBER";
	
	public static final String INTERVAL_REPORT="INTERVAL_REPORT";
 
	public static final String NOTIFICATION_ID="NOTIFICATION_ID";
	public static final String SEND_NOTIFICATION_ID="SEND_NOTIFICATION_ID";
	public static final String SIGNAL_FLARE_DATE="SIGNAL_FLARE_DATE";
	
	public static final String VERSION_PREY_DEFAULT="1.2.3";
	
	public static final String SEND_DATA="SEND_DATA";
	
	public static final String LAST_REPORT_START_DATE="LAST_REPORT_START_DATE";
	
	public static final String NEXT_ALERT="NEXT_ALERT";
	
	
	public static final String PREFS_DISABLE_POWER_OPTIONS="PREFS_DISABLE_POWER_OPTIONS";
	public static final String LOW_BATTERY_DATE="LOW_BATTERY_DATE";
	
	public static final String SCHEDULED="SCHEDULED";
	public static final String MINUTE_SCHEDULED="MINUTE_SCHEDULED";
	
	private boolean sendNotificationId;
	private String notificationId;

	public static final String TAG = "PREY";
	private static final String PICTURE_FILENAME = "PICTURE_FILENAME"; 

	private static PreyConfig cachedInstance = null;
	public static String postUrl = null;
	private String deviceID = "";
	private String apiKey = "";
	private boolean missing;
	private String email;
	private String destinationSms;
	 
	private boolean shouldCheckSimChange;
 
	private boolean isTheAccountAlreadyVerified;
	private boolean securityPrivilegesAlreadyPrompted;
	private boolean isCamouflageSet;
	private boolean locked;
	private boolean runOnce;
	private boolean froyoOrAbove;
	private boolean cupcakeOrAbove;
	private boolean gingerbreadOrAbove;
	private boolean kitKatOrAbove;
	private boolean jellyBeanOrAbove;
	private boolean iceCreamOrAbove;
	private boolean honeycombOrAbove;
	private boolean eclairOrAbove;
	
	private boolean camouflageSet;
	
	private String lastEvent;
	private String previousSsid;
	private boolean isRevokedPassword;
	private String revokedPassword;
	
	private long installationDate;
	private int flagFeedback;
	
	 
	
	private boolean run;
	private String simSerialNumber;
	
	private String version;

	private String intervalReport;
 
	private boolean registerC2dm=false;
	
	private long signalFlareDate;
	
	private boolean sendData;
	
	private boolean nextAlert; 
	
	private boolean disablePowerOptions;
	
	private long lowBatteryDate;
	
	private boolean scheduled;
	private int minuteScheduled;
	
	private Context ctx;

	private PreyConfig(Context ctx) {
		this.ctx = ctx;
		this.kitKatOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 19;
		this.jellyBeanOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 16;
		this.iceCreamOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 15;
		this.honeycombOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 13;
		this.gingerbreadOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 9;
		this.froyoOrAbove = Integer.parseInt(Build.VERSION.SDK) >= 8;
		this.eclairOrAbove = Integer.parseInt(Build.VERSION.SDK) >=5;
		this.cupcakeOrAbove = Integer.parseInt(Build.VERSION.SDK) == 3;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		settings.registerOnSharedPreferenceChangeListener(listener);
		this.deviceID = settings.getString(PreyConfig.PREFS_DEVICE_ID, "");
		this.apiKey = settings.getString(PreyConfig.PREFS_API_KEY, "");
		this.destinationSms = settings.getString(PreyConfig.PREFS_DESTINATION_SMS, "");
		this.missing = Boolean.valueOf(settings.getString(PreyConfig.PREFS_IS_MISSING, "false"));
		this.email = settings.getString(PreyConfig.PREFS_EMAIL, "");
		 
		this.shouldCheckSimChange = settings.getBoolean(PreyConfig.PREFS_CHECK_SIM_CHANGE, true);

		this.isCamouflageSet = settings.getBoolean(PreyConfig.IS_CAMOUFLAGE_SET, false);
		this.isTheAccountAlreadyVerified = settings.getBoolean(PreyConfig.PREFS_ACCOUNT_VERIFIED, false);
		this.securityPrivilegesAlreadyPrompted = settings.getBoolean(PreyConfig.PREFS_SECURITY_PROMPT_SHOWN, false);
		this.locked = settings.getBoolean(PreyConfig.IS_LOCK_SET, false);
		
		this.isRevokedPassword=settings.getBoolean(PreyConfig.IS_REVOKED_PASSWORD, false);
		this.revokedPassword=settings.getString(PreyConfig.REVOKED_PASSWORD, "");
		


		this.flagFeedback=settings.getInt(PreyConfig.FLAG_FEEDBACK, FeedbackActivity.FLAG_FEEDBACK_INIT);
		 
		
		this.previousSsid=settings.getString(PreyConfig.PREVIOUS_SSID, "");

		
		this.lastEvent=settings.getString(PreyConfig.LAST_EVENT, "");
		
		this.version=settings.getString(PreyConfig.VERSION, VERSION_V1);
		this.simSerialNumber=settings.getString(PreyConfig.SIM_SERIAL_NUMBER, "");
		this.intervalReport=settings.getString(PreyConfig.INTERVAL_REPORT, "");
		this.sendNotificationId=settings.getBoolean(PreyConfig.SEND_NOTIFICATION_ID,false);
		
		this.signalFlareDate=settings.getLong(PreyConfig.SIGNAL_FLARE_DATE, 0);
		
		this.installationDate=settings.getLong(PreyConfig.INSTALLATION_DATE, new Date().getTime());
		this.sendData=settings.getBoolean(PreyConfig.SEND_DATA, false);
		this.nextAlert=settings.getBoolean(PreyConfig.NEXT_ALERT, false);
		this.disablePowerOptions = settings.getBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, false);
		this.lowBatteryDate=settings.getLong(PreyConfig.LOW_BATTERY_DATE, 0);
		
		this.scheduled=settings.getBoolean(PreyConfig.SCHEDULED,FileConfigReader.getInstance(ctx).isScheduled());
		this.minuteScheduled=settings.getInt(PreyConfig.MINUTE_SCHEDULED,FileConfigReader.getInstance(ctx).getMinuteScheduled());
		
		saveLong(PreyConfig.INSTALLATION_DATE,installationDate);
	}
	
	public void saveAccount(PreyAccountData accountData) {
		PreyConfig.deleteCacheInstance();
		this.saveSimInformation();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PreyConfig.PREFS_DEVICE_ID, accountData.getDeviceId());
		editor.putString(PreyConfig.PREFS_API_KEY, accountData.getApiKey());
		editor.putString(PreyConfig.PREFS_EMAIL, accountData.getEmail());
		editor.putString(PreyConfig.PREFS_IS_MISSING, Boolean.valueOf(accountData.isMissing()).toString());

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
		if (cachedInstance != null)
			
			cachedInstance.deleteSmsPicture();
		cachedInstance = null;
	}
	
	SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(PREFS_DISABLE_POWER_OPTIONS)){
				disablePowerOptions = sharedPreferences.getBoolean(PREFS_DISABLE_POWER_OPTIONS, false);
				if(disablePowerOptions){
					ctx.startService(new Intent(ctx, PreyDisablePowerOptionsService.class));
				}else{
					ctx.stopService(new Intent(ctx, PreyDisablePowerOptionsService.class));
				}
			}
			
			if (key.equals(PREFS_SCHEDULED)){
				int value=Integer.parseInt(sharedPreferences.getString(PREFS_SCHEDULED,"0"));
				int valueOld=getMinuteScheduled();
				if(value!=valueOld){
					setScheduled(value>0);
					setMinuteScheduled(value);
					if (value>0){
						PreyScheduled.getInstance(ctx).run(value);
					}else{
						PreyScheduled.getInstance(ctx).reset();
					}
				}
			}
			PreyConfig.deleteCacheInstance();
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
		this.saveString(PreyConfig.PREFS_IS_MISSING, Boolean.valueOf(missing).toString());
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
	
	
	public void setRevokedPassword(boolean isRevokedPassword,String revokedPassword) {
		this.isRevokedPassword = isRevokedPassword;
		this.revokedPassword = revokedPassword;
		
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PreyConfig.IS_REVOKED_PASSWORD, isRevokedPassword);
		editor.putString(PreyConfig.REVOKED_PASSWORD, revokedPassword);
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
	
	public boolean isDisablePowerOptions(){
		return disablePowerOptions;
	}
	
	public boolean isLockSet() {
		return locked;
	}
	
	public boolean isCamouflageSet() {
		return isCamouflageSet;
	}

	public String getSmsToStop() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_SMS_STOP, "STOP PREY");
	}

	public String getSmsToRun() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_SMS_RUN, "GO PREY");
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
		/*
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
		}*/
			
		return isVerified;
	}
	
	public boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return deviceID!=null&&!"".equals(deviceID);
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
		TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String simSerial=telephonyManager.getSimSerialNumber();
		if(simSerial!=null){
			this.setSimSerialNumber(simSerial);
		}
		this.saveString(PreyConfig.PREFS_SIM_SERIAL_NUMBER, this.getSimSerialNumber());
		PreyLogger.d("SIM Serial number stored: " + this.getSimSerialNumber());
	}

 

	public boolean isSimChanged() {
		
			TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
			String simSerial=telephonyManager.getSimSerialNumber();
			PreyLogger.i("simSerial:"+simSerial+" actual:"+this.simSerialNumber);
			if (this.simSerialNumber==null||"".equals(this.simSerialNumber)){
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

	public String getDestinationSmsNumber() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_DESTINATION_SMS, "");
	}
	
	public String getDestinationSmsName() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREFS_DESTINATION_SMS_NAME, "");
	}

	public void saveDestinationSmsNumber(String destinationSms) {
		this.saveString(PreyConfig.PREFS_DESTINATION_SMS, destinationSms);
	}
	
	public void saveDestinationSmsName(String destinationSmsName) {
		this.saveString(PreyConfig.PREFS_DESTINATION_SMS_NAME, destinationSmsName);
	}
	
	public Bitmap getDestinationSmsPicture(){
		File sd = Environment.getExternalStorageDirectory();
		File dest = new File(sd, PICTURE_FILENAME);
		try {
			FileInputStream is = new FileInputStream(dest);
			BufferedInputStream bis = new BufferedInputStream(is);
			return BitmapFactory.decodeStream(bis);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public void saveDestinationSmsPicture(Bitmap detinationSmsPicture){
		File sd = Environment.getExternalStorageDirectory();
		File dest = new File(sd, PICTURE_FILENAME);

		try {
		     FileOutputStream out = new FileOutputStream(dest);
		     detinationSmsPicture.compress(Bitmap.CompressFormat.PNG, 90, out);
		     out.flush();
		     out.close();
		} catch (Exception e) {
		     e.printStackTrace();
		}
	}
	
	private void deleteSmsPicture(){
		File sd = Environment.getExternalStorageDirectory();
		File dest = new File(sd, PICTURE_FILENAME);
		dest.delete();
	}
	

	public void setSecurityPrivilegesAlreadyPrompted(boolean securityPrivilegesAlreadyPrompted) {
		this.securityPrivilegesAlreadyPrompted = securityPrivilegesAlreadyPrompted;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PreyConfig.PREFS_SECURITY_PROMPT_SHOWN, securityPrivilegesAlreadyPrompted);
		editor.commit();
	}
	
	
	
	
	public void registerC2dm(){
		if (PreyEmail.getEmail(this.ctx) != null) {
			PreyLogger.d("______________________");
			PreyLogger.d("______________________");
			PreyLogger.d("___ registerC2dm _____");
			PreyLogger.d("______________________");
			PreyLogger.d("______________________");
			PreyLogger.d("______________________");
			Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
			registrationIntent.putExtra("app", PendingIntent.getBroadcast(this.ctx, 0, new Intent(), 0)); // boilerplate
			String gcmId= FileConfigReader.getInstance(this.ctx).getGcmId();
			//PreyLogger.i("gcmId:"+gcmId);
			registrationIntent.putExtra("sender",gcmId);
			this.ctx.startService(registrationIntent);
		}
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
	
	private void saveInt(String key, int value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	private void saveBoolean(String key, boolean value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	private void saveLong(String key, long value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public boolean isRunOnce() {
		return runOnce;
	}

	public void setRunOnce(boolean runOnce) {
		this.runOnce = runOnce;
	}

	public boolean isFroyoOrAbove() {
		return froyoOrAbove;
	}

	public boolean isCupcakeOrAbove() {
		return cupcakeOrAbove;
	}

	public boolean isGingerbreadOrAbove() {
		return gingerbreadOrAbove;
	}
	
	public boolean isKitKatOrAbove() {
		return kitKatOrAbove;
	}
	public boolean isJellyBeanOrAbove() {
		return jellyBeanOrAbove;
	}
	public boolean isIceCreamOrAbove() {
		return iceCreamOrAbove;
	}
	public boolean isHoneycombOrAbove() {
		return honeycombOrAbove;
	}
	public boolean isEclairOrAbove() {
		return eclairOrAbove;
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
	
	public String getPreyMinorVersion() {
		return FileConfigReader.getInstance(this.ctx).getPreyMinorVersion();
	}
	
	public String getPreyDomain() {
		return FileConfigReader.getInstance(this.ctx).getPreyDomain();
	}
	
	public String getPreyCampaign() {
		return FileConfigReader.getInstance(this.ctx).getPreyCampaign();
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

	private static final String HTTP="https://";
	
	public String getPreyUrl() {
		String subdomain = FileConfigReader.getInstance(this.ctx).getPreySubdomain();
		return HTTP.concat(subdomain).concat(".").concat(getPreyDomain()).concat("/");
		
	}
	
	public String getPreyPanelUrl() {
		String panel = FileConfigReader.getInstance(this.ctx).getPreyPanel();
		String url=HTTP.concat(panel).concat(".").concat(getPreyDomain()).concat("/").concat(getPreyCampaign());
		PreyLogger.i(url);
		return url;
	}

	public boolean askForPassword() {
		boolean ask =  FileConfigReader.getInstance(this.ctx).isAskForPassword();
		PreyLogger.d("Ask for password?"+ask);
		return ask;
	}
	
	public boolean isLogEnabled() {
		return FileConfigReader.getInstance(this.ctx).isLogEnabled();
	}

	public boolean isRevokedPassword() {
		return isRevokedPassword;
	}

	public String getRevokedPassword() {
		return revokedPassword;
	}
	
	public int getFlagFeedback() {
		return flagFeedback;
	}
 
	public void setFlagFeedback(int flagFeedback) {
		this.flagFeedback=flagFeedback;
		this.saveInt(PreyConfig.FLAG_FEEDBACK,flagFeedback);
	}
	 
	public boolean showFeedback(){
		return FeedbackActivity.showFeedback(installationDate, flagFeedback);
	}
	
	public void setCamouflageSet(boolean camouflageSet){
		this.camouflageSet=camouflageSet;
		this.saveBoolean(PreyConfig.IS_CAMOUFLAGE_SET,camouflageSet);
	}
	
	public String getApiKeyBatch() {
		return FileConfigReader.getInstance(this.ctx).getApiKeyBatch();
	}

	public String getEmailBatch() {
		return FileConfigReader.getInstance(this.ctx).getEmailBatch();
	}
	
	
	public void setPreviousSsid(String previousSsid) {
		this.saveString(PreyConfig.PREVIOUS_SSID, previousSsid);
	}
	
	public String getPreviousSsid() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.PREVIOUS_SSID, previousSsid);
	}
	
	public String getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(String lastEvent) {
		this.lastEvent = lastEvent;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PreyConfig.LAST_EVENT, lastEvent);
		editor.commit();
		
	}
	
	public boolean getDisablePowerOptions() {
		return disablePowerOptions;
	}

	public void setDisablePowerOptions(boolean disablePowerOptions) {
		this.disablePowerOptions = disablePowerOptions;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PreyConfig.PREFS_DISABLE_POWER_OPTIONS, disablePowerOptions);
		editor.commit();
	}
	
	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
	public void setVersion(String version) {
		this.version=version;
		this.saveString(PreyConfig.VERSION,version);
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getSimSerialNumber(){
		return simSerialNumber;
	}
	
	public void setSimSerialNumber(String simSerialNumber) {
		this.simSerialNumber=simSerialNumber;
		this.saveString(PreyConfig.SIM_SERIAL_NUMBER,simSerialNumber);
	}
	
 
	public String getIntervalReport(){
		return intervalReport;
	}
	
	public void setIntervalReport(String intervalReport) {
		this.intervalReport=intervalReport;
		this.saveString(PreyConfig.INTERVAL_REPORT,intervalReport);
	}
	
	public void setNotificationId(String notificationId){
		this.notificationId=notificationId;
		saveString(PreyConfig.NOTIFICATION_ID, notificationId);
	}
	
	public String getNotificationId(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getString(PreyConfig.NOTIFICATION_ID, "");
	}
	
	public void setSendNotificationId(boolean sendNotificationId){
		this.sendNotificationId=sendNotificationId;
		saveBoolean(PreyConfig.SEND_NOTIFICATION_ID, sendNotificationId);
	}
	
	public boolean isSendNotificationId(){
		return this.sendNotificationId;
	}
	
	public long getInstallationDate(){
		return this.installationDate;
	}
	
	public void setSignalFlareDate(long signalFlareDate){
		this.signalFlareDate=signalFlareDate;
		saveLong(PreyConfig.SIGNAL_FLARE_DATE, this.signalFlareDate);
	}
	
	public long getLowBatteryDate(){
		return this.lowBatteryDate;
	}
	
	public void setLowBatteryDate(long lowBatteryDate){
		this.lowBatteryDate=lowBatteryDate;
		saveLong(PreyConfig.LOW_BATTERY_DATE, this.lowBatteryDate);
	}
	
	public long getSignalFlareDate(){
		return this.signalFlareDate;
	}
 
	public void setRegisterC2dm(boolean registerC2dm){
		this.registerC2dm=registerC2dm;
	}
	public boolean isRegisterC2dm(){
		return registerC2dm;
	}
	
	
	public boolean isSendData(){
		return sendData;
	}

	public void setSendData(boolean sendData) {
		this.sendData = sendData;
		this.saveBoolean(PreyConfig.SEND_DATA, sendData);
	}
	
	public void setLastReportStartDate(long lastReportStartDate){
		saveLong(PreyConfig.LAST_REPORT_START_DATE, lastReportStartDate);
	}
	
	public long getLastReportStartDate(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		return settings.getLong(PreyConfig.LAST_REPORT_START_DATE, 0);
	}
	
	public void setNextAlert(boolean nextAlert){
		this.nextAlert=nextAlert;
		saveBoolean(PreyConfig.NEXT_ALERT, nextAlert);
	}
	
	public boolean isNextAlert(){
		return nextAlert;
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
 
}
