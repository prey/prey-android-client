/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
 

 

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.prey.FileConfigReader;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.PreyPhone.Hardware;
import com.prey.PreyPhone.Wifi;
import com.prey.actions.HttpDataService;
import com.prey.backwardcompatibility.AboveCupcakeSupport;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.http.EntityFile;
import com.prey.R;
/**
 * This class has the web services implementation needed to connect with prey
 * web services
 * 
 * @author cyaconi
 * 
 */
public class PreyWebServices {

	private static PreyWebServices _instance = null;
	//private static String preyURL = null;

	private PreyWebServices() {

	}

	public static PreyWebServices getInstance() {
		if (_instance == null)
			_instance = new PreyWebServices();
		return _instance;
	}

	/**
	 * Register a new account and get the API_KEY as return In case email is
	 * already registered, this service will return an error.
	 * 
	 * @throws PreyException
	 * 
	 */
	public PreyAccountData registerNewAccount(Context ctx, String name, String email, String password, String deviceType) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("user[name]", name);
		parameters.put("user[email]", email);
		parameters.put("user[password]", password);
		parameters.put("user[password_confirmation]", password);
		parameters.put("user[referer_user_id]", "");
		parameters.put("user[country_name]", Locale.getDefault().getDisplayCountry());
		parameters.put("agreement[key]", PreyConfig.getPreyConfig(ctx).getAgreementId());
		
		PreyHttpResponse response=null;
		String xml;
		try {
			response=PreyRestHttpClient.getInstance(ctx).post(PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("users.xml"), parameters, preyConfig);
			xml = response.getResponseAsString();
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
		checkForError(xml);

		int from;
		int to;
		String apiKey;
		try {
			from = xml.indexOf("<key>") + 5;
			to = xml.indexOf("</key>");
			apiKey = xml.substring(from, to);
		} catch (Exception e) { 
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));
		}

		String xmlDeviceId = this.registerNewDevice(ctx, apiKey, deviceType);
		checkForError(xmlDeviceId);
		from = xmlDeviceId.indexOf("<key>") + 5;
		to = xmlDeviceId.indexOf("</key>");

		String deviceId = null;
		try {
			deviceId = xmlDeviceId.substring(from, to);
		} catch (Exception e) {
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));
		}

		PreyAccountData newAccount = new PreyAccountData();
		newAccount.setApiKey(apiKey);
		newAccount.setDeviceId(deviceId);
		newAccount.setEmail(email);
		newAccount.setPassword(password);
		newAccount.setName(name);
		return newAccount;
	}

	private void checkForError(String xml) throws PreyException {
		if (xml.contains("errors")) {
			int errorFrom = xml.indexOf("<error>") + 7;
			int erroTo = xml.indexOf("</error>");
			String errorMsg = xml.substring(errorFrom, erroTo);
			throw new PreyException(errorMsg); //
		}

	}

	/**
	 * Register a new device for a given API_KEY, needed just after obtain the
	 * new API_KEY.
	 * 
	 * @throws PreyException
	 */
	public String registerNewDevice(Context ctx, String api_key, String deviceType) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		
		String model = Build.MODEL;
		String vendor = "Google";
		if (!PreyConfig.getPreyConfig(ctx).isCupcake())
			vendor = AboveCupcakeSupport.getDeviceVendor();
		
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("api_key", api_key);
		parameters.put("device[title]", vendor + " " + model);
		parameters.put("device[device_type]", deviceType);
		parameters.put("device[os]", "Android");
		parameters.put("device[os_version]", Build.VERSION.RELEASE);
		parameters.put("device[referer_device_id]", "");
		//parameters.put("device[state]", "ok");
		parameters.put("device[plan]", "free");
		parameters.put("device[activation_phrase]", preyConfig.getSmsToRun());
		parameters.put("device[deactivation_phrase]", preyConfig.getSmsToStop());
		parameters.put("device[model_name]", model);
		parameters.put("device[vendor_name]", vendor);
		
		parameters=increaseData(ctx,parameters);
		TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		//String imsi = mTelephonyMgr.getSubscriberId();
		String imei = mTelephonyMgr.getDeviceId();
		parameters.put("device[physical_address]", imei);

		PreyHttpResponse response = null;
		try {
			response = PreyRestHttpClient.getInstance(ctx).post(PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("devices.xml"), parameters, preyConfig);
			// No more devices allowed
			if ((response.getStatusLine().getStatusCode() == 302) || (response.getStatusLine().getStatusCode() == 422)) {
				throw new NoMoreDevicesAllowedException(ctx.getText(R.string.set_old_user_no_more_devices_text).toString());
			}
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}

		return response.getResponseAsString();
	}

	public PreyAccountData registerNewDeviceToAccount(Context ctx, String email, String password, String deviceType) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();
		PreyHttpResponse response=null;
		String xml;
		try {
			response=PreyRestHttpClient.getInstance(ctx).get(PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("profile.xml"), parameters, preyConfig, email, password);
			xml = response.getResponseAsString(); 
		} catch (IOException e) {
			PreyLogger.e("Error!",e);
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}

		if (!xml.contains("<key"))
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));
		//

		int from;
		int to;
		String apiKey;
		try {
			from = xml.indexOf("<key>") + 5;
			to = xml.indexOf("</key>");
			apiKey = xml.substring(from, to);
		} catch (Exception e) {
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));
		}

		String xmlDeviceId = this.registerNewDevice(ctx, apiKey, deviceType);

		if (!xmlDeviceId.contains("<key"))
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));

		from = xmlDeviceId.indexOf("<key>") + 5;
		to = xmlDeviceId.indexOf("</key>");
		String deviceId = xmlDeviceId.substring(from, to);

		PreyAccountData newAccount = new PreyAccountData();
		newAccount.setApiKey(apiKey);
		newAccount.setDeviceId(deviceId);
		newAccount.setEmail(email);
		newAccount.setPassword(password);
		return newAccount;

	}

	public String sendPreyHttpReport(Context ctx, ArrayList<HttpDataService> dataToSend) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		
		Map<String, String> parameters = new HashMap<String, String>();
		List<EntityFile> entityFiles=new ArrayList<EntityFile>();
		for (HttpDataService httpDataService : dataToSend) {
			if (httpDataService!=null){
				parameters.putAll(httpDataService.getDataAsParameters());
				if (httpDataService.getEntityFiles()!=null&&httpDataService.getEntityFiles().size()>0){
					entityFiles.addAll(httpDataService.getEntityFiles());
				}
			}
		}

		parameters.put("api_key", preyConfig.getApiKey());

		String response = null;
		try {
			String URL = PreyConfig.postUrl != null ? PreyConfig.postUrl : this.getDeviceWebControlPanelUrl(ctx).concat("/reports.xml");
			PreyConfig.postUrl = null;
			if (entityFiles.size()==0)
				response = PreyRestHttpClient.getInstance(ctx).post(URL, parameters, preyConfig).getResponseAsString();
			else
				response = PreyRestHttpClient.getInstance(ctx).post(URL, parameters, preyConfig,entityFiles).getResponseAsString();
			PreyLogger.i("Report sent: " + response);
			try{
				GoogleAnalyticsTracker.getInstance().trackEvent("Report","Sent", "", 1);
			}catch(NullPointerException ex){
				GoogleAnalyticsTracker.getInstance().startNewSession(FileConfigReader.getInstance(ctx).getAnalyticsUA(),ctx);
				GoogleAnalyticsTracker.getInstance().trackEvent("Report","Sent", "", 1);
			}
			if (preyConfig.isShouldNotify()) {
				this.notifyUser(ctx);
			}
		} catch (Exception e) {
			PreyLogger.e("Report wasn't send",e);
		}
		return response;
	}

	private void notifyUser(Context ctx) {
		String notificationTitle = ctx.getText(R.string.notification_title).toString();
		NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.prey_status_bar_icon, notificationTitle, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(PreyConfig.CONTROL_PANEL_URL));
		String notificationToShow = ctx.getText(R.string.notification_msg).toString();
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, browserIntent, 0);
		notification.contentIntent = contentIntent;
		notification.setLatestEventInfo(ctx, ctx.getText(R.string.notification_title), notificationToShow, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later
		// to cancel.
		nm.notify(R.string.preyForAndroid_name, notification);

	}

	public void setMissing(Context ctx, boolean isMissing) {
		final PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("api_key", preyConfig.getApiKey());
		if (isMissing)
			parameters.put("device[missing]", "1");
		else
			parameters.put("device[missing]", "0");

		try {
			PreyRestHttpClient.getInstance(ctx).methodAsParameter(getDeviceUrl(ctx),"PUT", parameters, preyConfig);
		} catch (Exception e) {
			PreyLogger.e("Couldn't update missing state", e);
		}
	}


	public void setPushRegistrationId(Context ctx, String regId) {
		this.updateDeviceAttribute(ctx, "notification_id", regId);
		PreyLogger.d("c2dm registry id set succesfully");
	}
	
	public void updateActivationPhrase(Context ctx, String activationPhrase) {
		this.updateDeviceAttributeUi(ctx, "activation_phrase", activationPhrase);
	}
	
	public void updateDeactivationPhrase(Context ctx, String deactivationPhrase) {
		this.updateDeviceAttributeUi(ctx, "deactivation_phrase", deactivationPhrase);
	}
	
	private void updateDeviceAttribute(Context ctx, String key, String value){
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("api_key", preyConfig.getApiKey());
		parameters.put("device["+key+"]", value);

		try {
			PreyRestHttpClient.getInstance(ctx).methodAsParameter(this.getDeviceUrl(ctx),"PUT", parameters, preyConfig);
			PreyLogger.d("Update device attribute ["+ key + "] with value: " + value);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PreyException e) {
			PreyLogger.e("Attribute ["+key+"] wasn't updated to ["+value+"]", e);
		}
		
	}
	
	private void updateDeviceAttributeUi(Context ctx, String key, String value){
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("api_key", preyConfig.getApiKey());
		parameters.put("device["+key+"]", value);

		try {
			PreyRestHttpClient.getInstance(ctx).methodAsParameter(this.getDeviceUiUrl(ctx),"PUT", parameters, preyConfig);
			PreyLogger.d("Update device attribute ["+ key + "] with value: " + value);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PreyException e) {
			PreyLogger.e("Attribute ["+key+"] wasn't updated to ["+value+"]", e);
		}
		
	}

	public boolean checkPassword(Context ctx, String email, String password) throws PreyException {
		String xml = this.checkPassword(email, password, ctx);
		return xml.contains("<key");
	}

	public String checkPassword(String email, String password, Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();
		String xml;
		try {
			xml = PreyRestHttpClient.getInstance(ctx).get(PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("profile.xml"), parameters, preyConfig, email, password)
					.getResponseAsString();
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}

		return xml;
	}

	public String deleteDevice(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();
		String xml;
		try {
			xml = PreyRestHttpClient.getInstance(ctx)
					.delete(this.getDeviceUiUrl(ctx), parameters, preyConfig)
					.getResponseAsString();

		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
		return xml;
	}

	public void changePassword(Context ctx, String email, String currentPassword, String newPassword) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String userXml = this.checkPassword(email, currentPassword, ctx);
		if (!userXml.contains("<key"))
			throw new PreyException(ctx.getText(R.string.error_registered_password_has_changed).toString());

		int from = userXml.indexOf("<id>") + 4;
		int to = userXml.indexOf("</id>");
		String userId = userXml.substring(from, to);

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("user[password]", newPassword);
		parameters.put("user[password_confirmation]", newPassword);
		String xml;
		try {
			xml = PreyRestHttpClient
					.getInstance(ctx)
					.methodAsParameter(PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("users/").concat(userId).concat(".xml"), "PUT", parameters, preyConfig,
							preyConfig.getApiKey(), "X").getResponseAsString();
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
		checkForError(xml);
	}

	public String getActionsToPerform(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);

		Map<String, String> parameters = new HashMap<String, String>();
		try {
			return PreyRestHttpClient.getInstance(ctx).get(this.getDeviceUrl(ctx), parameters, preyConfig).getResponseAsString();
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
	}

	public boolean forgotPassword(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String URL = PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("forgot");
		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("user[email]", preyConfig.getEmail());

		try {
			PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).post(URL, parameters, preyConfig);
			if (response.getStatusLine().getStatusCode() != 302) {
				throw new PreyException(ctx.getText(R.string.error_cant_report_forgotten_password).toString());
			}
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_cant_report_forgotten_password).toString(), e);
		}

		return true;
	}
	
	public void deactivateModules(Context ctx, ArrayList<String> modules){
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();
		for (String module : modules) {
			parameters.put("device[deactivate_modules][]", module);
		}
		parameters.put("api_key", preyConfig.getApiKey());
		try {
			PreyRestHttpClient.getInstance(ctx).methodAsParameter(this.getDeviceUrl(ctx),"PUT", parameters, preyConfig);
			PreyLogger.d("Modules deactivation instruction sent");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PreyException e) {
			PreyLogger.e("Modules weren't deactivated", e);
		}
		
	}
	
	public static String getDeviceWebControlPanelUrl(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String deviceKey = preyConfig.getDeviceID();
		if (deviceKey == null || deviceKey == "")
			throw new PreyException("Device key not found on the configuration");
		return PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("devices/").concat(deviceKey);
	}
	
	public String getDeviceWebControlPanelUiUrl(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String deviceKey = preyConfig.getDeviceID();
		if (deviceKey == null || deviceKey == "")
			throw new PreyException("Device key not found on the configuration");
		return PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("devices/").concat(deviceKey);
	}
	
	 public static String getFileUrlJson(Context ctx) throws PreyException{
         return getDeviceUrl2(ctx).concat("/files");
	 }
	 
	 public static String getDeviceUrl2(Context ctx) throws PreyException{
         PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
         String deviceKey = preyConfig.getDeviceID();
         if (deviceKey == null || deviceKey == "")
                 throw new PreyException("Device key not found on the configuration");
         String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
         String url=PreyConfig.getPreyConfig(ctx).getPreyUrl2().concat(apiv2).concat("devices/").concat(deviceKey);
         return url;
 }
	 
	 
	private static String getDeviceUrl(Context ctx) throws PreyException{
		return getDeviceWebControlPanelUrl(ctx).concat(".xml");
	}
	
	private String getDeviceUiUrl(Context ctx) throws PreyException{
		return this.getDeviceWebControlPanelUiUrl(ctx).concat(".xml");
	}
	
	public HashMap<String, String> increaseData(Context ctx, HashMap<String, String> parameters) {
		PreyPhone phone = new PreyPhone(ctx);
		Hardware hardware = phone.getHardware();
		String prefix = "device[hardware_attributes]";
		parameters.put(prefix + "[uuid]", hardware.getUuid());
		parameters.put(prefix + "[bios_vendor]", hardware.getBiosVendor());
		parameters.put(prefix + "[bios_version]", hardware.getBiosVersion());
		parameters.put(prefix + "[mb_vendor]", hardware.getMbVendor());
		parameters.put(prefix + "[mb_serial]", hardware.getMbSerial());
		parameters.put(prefix + "[mb_model]", hardware.getMbModel());
		// parameters.put(prefix + "[mb_version]", hardware.getMbVersion());
		parameters.put(prefix + "[cpu_model]", hardware.getCpuModel());
		parameters.put(prefix + "[cpu_speed]", hardware.getCpuSpeed());
		parameters.put(prefix + "[cpu_cores]", hardware.getCpuCores());
		parameters.put(prefix + "[ram_size]", hardware.getRamSize());
		parameters.put(prefix + "[serial_number]", hardware.getSerialNumber());
		// parameters.put(prefix + "[ram_modules]", hardware.getRamModules());
		int nic = 0;
		Wifi wifi = phone.getWifi();
		if (wifi!=null){
			prefix = "device[hardware_attributes][network]";
			parameters.put(prefix + "[nic_" + nic + "][name]", wifi.getName());
			parameters.put(prefix + "[nic_" + nic + "][interface_type]", wifi.getInterfaceType());
			// parameters.put(prefix + "[nic_" + nic + "][model]", wifi.getModel());
			// parameters.put(prefix + "[nic_" + nic + "][vendor]", wifi.getVendor());
			parameters.put(prefix + "[nic_" + nic + "][ip_address]", wifi.getIpAddress());
			parameters.put(prefix + "[nic_" + nic + "][gateway_ip]", wifi.getGatewayIp());
			parameters.put(prefix + "[nic_" + nic + "][netmask]", wifi.getNetmask());
			parameters.put(prefix + "[nic_" + nic + "][mac_address]", wifi.getMacAddress());
		}
		return parameters;
	}
	

}
