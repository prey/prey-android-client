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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
 

 


import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.prey.FileConfigReader;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.PreyPhone.Hardware;
import com.prey.PreyPhone.Wifi;
import com.prey.actions.HttpDataService;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.observer.ActionsController;
import com.prey.backwardcompatibility.AboveCupcakeSupport;
import com.prey.events.Event;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.json.parser.JSONParser;
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
		///PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);

		HashMap<String, String> parameters = new HashMap<String, String>();
		/* 
		parameters.put("user[name]", name);
		parameters.put("user[email]", email);
		parameters.put("user[password]", password);
		parameters.put("user[password_confirmation]", password);
		parameters.put("user[referer_user_id]", "");
		parameters.put("user[country_name]", Locale.getDefault().getDisplayCountry());
		  */
		
		parameters.put("name", name);
		parameters.put("email", email);
		parameters.put("password", password);
		parameters.put("password_confirmation", password);
		parameters.put("country_name", Locale.getDefault().getDisplayCountry());

		
		PreyHttpResponse response=null;
		String xml;
		try {
			String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
			String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("signup.json");
			//String url=PreyConfig.getPreyConfig(ctx).getPreyUiUrl().concat("users.xml");
			response=PreyRestHttpClient.getInstance(ctx).post(url, parameters);
			xml = response.getResponseAsString();
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
		
		String apiKey="";
		if (xml.contains("\"key\"") ){
			try{
				JSONObject jsnobject = new JSONObject(xml);
				apiKey=jsnobject.getString("key");
			}catch(Exception e){
				
			}
		} else{
			if (response!=null&&response.getStatusLine()!=null&&response.getStatusLine().getStatusCode()>299){
				throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));
			}else{	
				throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,""));		
			}
		}

		PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType);
		String xmlDeviceId=responseDevice.getResponseAsString();
		String deviceId = null;
		if (xmlDeviceId.contains("{\"key\"") ){
			try{
				JSONObject jsnobject = new JSONObject(xmlDeviceId);
				deviceId=jsnobject.getString("key");
			}catch(Exception e){
				
			}
		}else{
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,""));		
		}

		PreyAccountData newAccount = new PreyAccountData();
		newAccount.setApiKey(apiKey);
		newAccount.setDeviceId(deviceId);
		newAccount.setEmail(email);
		newAccount.setPassword(password);
		newAccount.setName(name);
		return newAccount;
	}

	/*
	private void checkForError(String xml) throws PreyException {
		if (xml!=null&&xml.contains("errors")) {
			int errorFrom = xml.indexOf("<error>") + 7;
			int erroTo = xml.indexOf("</error>");
			String errorMsg = xml.substring(errorFrom, erroTo);
			throw new PreyException(errorMsg); //
		}

	}*/

	/**
	 * Register a new device for a given API_KEY, needed just after obtain the
	 * new API_KEY.
	 * 
	 * @throws PreyException
	 */
	private PreyHttpResponse registerNewDevice(Context ctx, String api_key, String deviceType) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		
		String model = Build.MODEL;
		String vendor = "Google";
		if (!PreyConfig.getPreyConfig(ctx).isCupcakeOrAbove())
			vendor = AboveCupcakeSupport.getDeviceVendor();
		
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("api_key", api_key);
		parameters.put("title", vendor + " " + model);
		parameters.put("device_type", deviceType);
		parameters.put("os", "Android");
		parameters.put("os_version", Build.VERSION.RELEASE);
		parameters.put("referer_device_id", "");
		parameters.put("plan", "free");
		parameters.put("activation_phrase", preyConfig.getSmsToRun());
		parameters.put("deactivation_phrase", preyConfig.getSmsToStop());
		parameters.put("model_name", model);
		parameters.put("vendor_name", vendor);
		
		parameters=increaseData(ctx,parameters);
		TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		//String imsi = mTelephonyMgr.getSubscriberId();
		String imei = mTelephonyMgr.getDeviceId();
		parameters.put("physical_address", imei);

		PreyHttpResponse response = null;
		try {
			String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
			String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices.json"); 
			PreyLogger.d("url:"+url);
			response = PreyRestHttpClient.getInstance(ctx).post(url, parameters);
			PreyLogger.d("response:"+response.getStatusLine() +" "+ response.getResponseAsString());
			// No more devices allowed
			
			if ((response.getStatusLine().getStatusCode() == 302) || (response.getStatusLine().getStatusCode() == 422)||(response.getStatusLine().getStatusCode() == 403)) {
				throw new NoMoreDevicesAllowedException(ctx.getText(R.string.set_old_user_no_more_devices_text).toString());
			}
			if (response.getStatusLine().getStatusCode()>299){
				throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,"["+response.getStatusLine().getStatusCode()+"]"));
			}
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}

		return response;
	}

	public PreyAccountData registerNewDeviceToAccount(Context ctx, String email, String password, String deviceType) throws PreyException {
		PreyLogger.d("email:"+email+" password:"+password);
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		HashMap<String, String> parameters = new HashMap<String, String>();
		PreyHttpResponse response=null;
		String xml;
		try {
			String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
			String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("profile.xml");
			PreyLogger.d("url:"+url);
			response=PreyRestHttpClient.getInstance(ctx).get(url, parameters, preyConfig, email, password);
			xml = response.getResponseAsString(); 
		} catch (IOException e) {
			PreyLogger.e("Error!",e);
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
		String status="";
		if(response!=null&&response.getStatusLine()!=null){
			status="["+response.getStatusLine().getStatusCode()+"]";
		}
		if (!xml.contains("<key")){
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,status));
		}

		int from;
		int to;
		String apiKey;
		try {
			from = xml.indexOf("<key>") + 5;
			to = xml.indexOf("</key>");
			apiKey = xml.substring(from, to);
		} catch (Exception e) {
			throw new PreyException(ctx.getString(R.string.error_cant_add_this_device,status));
		}
		String deviceId =null;
		PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType);
		String xmlDeviceId=responseDevice.getResponseAsString();
		//if json
		if (xmlDeviceId.contains("{\"key\"") ){
			try{
				JSONObject jsnobject = new JSONObject(xmlDeviceId);
				deviceId=jsnobject.getString("key");
			}catch(Exception e){
				
			}
		}
		PreyAccountData newAccount = new PreyAccountData();
		newAccount.setApiKey(apiKey);
		newAccount.setDeviceId(deviceId);
		newAccount.setEmail(email);
		newAccount.setPassword(password);
		return newAccount;

	}
	
	public PreyAccountData registerNewDeviceWithApiKeyEmail(Context ctx, String apiKey,String email, String deviceType) throws PreyException {
		String deviceId ="";
		PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType);
		String xmlDeviceId=responseDevice.getResponseAsString();
		//if json
		if (xmlDeviceId.contains("{\"key\"") ){
			try{
				JSONObject jsnobject = new JSONObject(xmlDeviceId);
				deviceId=jsnobject.getString("key");
				}catch(Exception e){
			}
		}
		PreyAccountData newAccount = new PreyAccountData();
		newAccount.setApiKey(apiKey);
		newAccount.setDeviceId(deviceId);
		newAccount.setEmail(email);
		newAccount.setPassword("");
		return newAccount;

	}

	public PreyHttpResponse setPushRegistrationId(Context ctx, String regId) {
		//this.updateDeviceAttribute(ctx, "notification_id", regId);
		HttpDataService data = new HttpDataService("notification_id");
	    data.setList(false);
		data.setKey("notification_id");
		data.setSingleData(regId);
		ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
		dataToBeSent.add(data);
		PreyHttpResponse response=PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
		if (response!=null&&response.getStatusLine()!=null&&response.getStatusLine().getStatusCode()==200){
			PreyLogger.d("c2dm registry id set succesfully");
		}
		return response;
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
			xml = PreyRestHttpClient.getInstance(ctx).get(PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("profile.xml"), parameters, preyConfig, email, password)
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
			String url=this.getDeviceWebControlPanelUiUrl(ctx);
			PreyHttpResponse response=PreyRestHttpClient.getInstance(ctx)
					.delete(url, parameters, preyConfig);
			PreyLogger.d(response.toString());
			xml = response.getResponseAsString();

		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}
		return xml;
	}

	public boolean forgotPassword(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String URL = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("forgot");
		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("user[email]", preyConfig.getEmail());

		try {
			PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).post(URL, parameters);
			if (response.getStatusLine().getStatusCode() != 302) {
				throw new PreyException(ctx.getText(R.string.error_cant_report_forgotten_password).toString());
			}
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_cant_report_forgotten_password).toString(), e);
		}

		return true;
	}
	
	public static String getDeviceWebControlPanelUrl(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String deviceKey = preyConfig.getDeviceID();
		if (deviceKey == null || deviceKey == "")
			throw new PreyException("Device key not found on the configuration");
		String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
		//apiv2="";
		return PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
	}
	
	public String getDeviceWebControlPanelUiUrl(Context ctx) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String deviceKey = preyConfig.getDeviceID();
		if (deviceKey == null || deviceKey == "")
			throw new PreyException("Device key not found on the configuration");
		String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
		return PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
	}
	
	
	private String getDeviceUrlJson(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat(".json");
	}
	
	private String getVerifyUrl(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat("/verify.json");
	}
	
	 private String getReportUrlJson(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat("/reports.json");  
	 }
		
	 public String getFileUrlJson(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat("/files");
	 }
		
	 public String getDataUrlJson(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat("/data.json");
	 }
		
	 private String getEventsUrlJson(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat("/events");
	 }
		
	 private String getResponseUrlJson(Context ctx) throws PreyException{
		 return getDeviceUrlApiv2(ctx).concat("/response");
	 }
	 
	private String getDeviceUrlApiv2(Context ctx) throws PreyException{
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			String deviceKey = preyConfig.getDeviceID();
			if (deviceKey == null || deviceKey == "")
				throw new PreyException("Device key not found on the configuration");
			//String apiv=FileConfigReader.getInstance(ctx).getApiV1();
			String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
			String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
			return url;
	}
	
	public  String getDeviceUrlV2(Context ctx) throws PreyException{
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String deviceKey = preyConfig.getDeviceID();
		if (deviceKey == null || deviceKey == "")
			throw new PreyException("Device key not found on the configuration");
		String apiv2=FileConfigReader.getInstance(ctx).getApiV2();
		String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
		return url;
	}
	
	public  String getDeviceUrl(Context ctx) throws PreyException{
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String deviceKey = preyConfig.getDeviceID();
		if (deviceKey == null || deviceKey == "")
			throw new PreyException("Device key not found on the configuration");
		 
		String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("devices/").concat(deviceKey);
		return url;
	}

	
	public HashMap<String, String> increaseData(Context ctx, HashMap<String, String> parameters) {
		PreyPhone phone = new PreyPhone(ctx);
		Hardware hardware = phone.getHardware();
		String prefix = "hardware_attributes";
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
		parameters.put(prefix + "[ram_size]", ""+hardware.getTotalMemory());
		parameters.put(prefix + "[serial_number]", hardware.getSerialNumber());
		// parameters.put(prefix + "[ram_modules]", hardware.getRamModules());
		int nic = 0;
		Wifi wifi = phone.getWifi();
		if (wifi!=null){
			prefix = "hardware_attributes[network]";
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
	

	public PreyHttpResponse sendPreyHttpData(Context ctx, ArrayList<HttpDataService> dataToSend) {
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
		Hardware hardware=new PreyPhone(ctx).getHardware();
		if(!PreyConfig.getPreyConfig(ctx).isSendData()&&hardware.getTotalMemory()>0){
			PreyConfig.getPreyConfig(ctx).setSendData(true);
			parameters.put("hardware_attributes[ram_size]",""+hardware.getTotalMemory());
		}
		
		
	//	parameters.put("notification_id", preyConfig.getNotificationId());

 
		PreyHttpResponse preyHttpResponse=null;
		try {
			String url =getDataUrlJson(ctx);
			PreyLogger.d("URL:"+url);
			PreyConfig.postUrl = null;
			
			
			
			if (entityFiles.size()==0)
				preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig);
			else
				preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig,entityFiles);
			//PreyLogger.d("Data sent_: " + preyHttpResponse.getResponseAsString());
		} catch (Exception e) {
			PreyLogger.e("Data wasn't send",e);
		} 
		return preyHttpResponse;
	}
	
	
	public boolean verify(Context ctx) throws PreyException, IOException{
		boolean result=false;
		String url =getVerifyUrl(ctx);
		//PreyLogger.i("verify url:"+url);
		PreyHttpResponse preyHttpResponse  =null;
		PreyConfig config=PreyConfig.getPreyConfig(ctx);
		preyHttpResponse  =PreyRestHttpClient.getInstance(ctx).get(url, null, config,config.getApiKey(),"X");
		//PreyLogger.d("status:"+preyHttpResponse.getStatusLine().getStatusCode());
		result = (preyHttpResponse.getStatusLine().getStatusCode()==200);
		return result;
	}

	public void sendPreyHttpEvent(Context ctx, Event event, JSONObject jsonObject){
		try {
			String url =getEventsUrlJson(ctx)+".json";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("name", event.getName());
			parameters.put("info", event.getInfo());
			
			PreyLogger.d("sendPreyHttpEvent url:"+url);
			PreyLogger.d("name:"+event.getName()+" info:"+event.getInfo());
			
			//Toast.makeText(ctx, "Event:"+event.getName(), Toast.LENGTH_LONG).show();
			String status=jsonObject.toString();
			PreyHttpResponse preyHttpResponse= PreyRestHttpClient.getInstance(ctx).postStatusAutentication(url, status, parameters, PreyConfig.getPreyConfig(ctx));
			runActionJson(ctx,preyHttpResponse);
		} catch (Exception e) {
			PreyLogger.i("message:"+e.getMessage());
			PreyLogger.e("Event wasn't send",e);
		} 
	}
	
	public void runActionJson(Context ctx,PreyHttpResponse preyHttpResponse) throws Exception{
		StringBuilder jsonString=PreyRestHttpClient.getInstance(ctx).getStringHttpResponse(preyHttpResponse.getResponse());
		if (jsonString!=null&&jsonString.length()>0){
			List<JSONObject> jsonObjectList=new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
			if (jsonObjectList!=null&&jsonObjectList.size()>0){
				ActionsController.getInstance(ctx).runActionJson(ctx,jsonObjectList);
			}
		}
	}
	
	public void postData(String url,JSONObject obj) {


	    HttpParams myParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(myParams, 10000);
	    HttpConnectionParams.setSoTimeout(myParams, 10000);
	    HttpClient httpclient = new DefaultHttpClient(myParams);
	    String json=obj.toString();

	    try {

	        HttpPost httppost = new HttpPost(url.toString());
	        httppost.setHeader("Content-type", "application/json");

	        StringEntity se = new StringEntity(json); 
	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        httppost.setEntity(se); 

	        HttpResponse response = httpclient.execute(httppost);
	        String temp = EntityUtils.toString(response.getEntity());
	        PreyLogger.d("tag"+ temp);


	    } catch (ClientProtocolException e) {

	    } catch (IOException e) {
	    }
	}
	
	public String sendNotifyActionResultPreyHttp(Context ctx,   Map<String, String> params) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		String response = null;   
		try {			
			String url=getResponseUrlJson(ctx);
			PreyConfig.postUrl = null;
			PreyHttpResponse httpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, params,preyConfig);
			response=httpResponse.toString();
			PreyLogger.d("Notify Action Result sent: " + response);
		} catch (Exception e) {
			//PreyLogger.e("Notify Action Result wasn't send",e);
		}  
		return response;
	} 
	
	public PreyHttpResponse sendPreyHttpReport(Context ctx, List<HttpDataService> dataToSend) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		
		Map<String, String> parameters = new HashMap<String, String>();
		List<EntityFile> entityFiles=new ArrayList<EntityFile>();
		for (HttpDataService httpDataService : dataToSend) {
			if (httpDataService!=null){
				parameters.putAll(httpDataService.getReportAsParameters());
				if (httpDataService.getEntityFiles()!=null&&httpDataService.getEntityFiles().size()>0){
					entityFiles.addAll(httpDataService.getEntityFiles());
				}
			}
		}

		//parameters.put("api_key", preyConfig.getApiKey());

	 
		PreyHttpResponse preyHttpResponse=null;
		try {
			String url =getReportUrlJson(ctx);
			PreyConfig.postUrl = null;
			PreyLogger.d("report url:"+url);
			
			
			if (entityFiles==null||entityFiles.size()==0)
				preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig);
			else
				preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig,entityFiles);
			PreyLogger.i("Report sent: " + preyHttpResponse.getResponseAsString());
		} catch (Exception e) {
			PreyLogger.e("Report wasn't send:"+e.getMessage(),e);
		} 
		return preyHttpResponse;
	}
	
	public List<JSONObject> getActionsJsonToPerform(Context ctx) throws PreyException {
		String url=getDeviceUrlJson(ctx);
		//PreyLogger.i("url:"+url);
		List<JSONObject> lista=new JSONParser().getJSONFromUrl(ctx,url); 
	 
		return lista;
	}
	
	public PreyHttpResponse registerNewDeviceRemote(Context ctx,String mail,String notificationId,String deviceType) throws PreyException {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		
		String model = Build.MODEL;
		String vendor = "Google";
		if (!PreyConfig.getPreyConfig(ctx).isCupcakeOrAbove())
			vendor = AboveCupcakeSupport.getDeviceVendor();
		
		
		
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("device[notification_id]", notificationId);
		parameters.put("device[remote_email]", mail);
		parameters.put("device[title]", vendor + " " + model);
		parameters.put("device[device_type]", deviceType);
		parameters.put("device[os]", "Android");
		parameters.put("device[os_version]", Build.VERSION.RELEASE);
		parameters.put("device[referer_device_id]", "");
		parameters.put("device[plan]", "free");
		parameters.put("device[activation_phrase]", preyConfig.getSmsToRun());
		parameters.put("device[deactivation_phrase]", preyConfig.getSmsToStop());
		parameters.put("device[model_name]", model);
		parameters.put("device[vendor_name]", vendor);
		
		parameters=increaseData(ctx,parameters);
		TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		parameters.put("device[physical_address]", imei);

		PreyHttpResponse response = null;
		try {
			String url="https://panel.preyapp.com/api/v2/remote.json";
			response = PreyRestHttpClient.getInstance(ctx).post(url, parameters);
		} catch (IOException e) {
			throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
		}

		return response;
	}
	
	
	public PreyHttpResponse sendContact(Context ctx, HashMap<String, String> parameters) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);


 
		PreyHttpResponse preyHttpResponse=null;
		try {

			String url=getDeviceUrlApiv2(ctx).concat("/contacts");
			
			PreyConfig.postUrl = null;
			
			
			
			 
				preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig);
		 
		 
			 
		} catch (Exception e) {
			PreyLogger.e("Contact wasn't send",e);
		} 
		return preyHttpResponse;
	}
	
	public PreyHttpResponse sendBrowser(Context ctx, HashMap<String, String> parameters) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		PreyHttpResponse preyHttpResponse=null;
		try {
			String url=getDeviceUrlApiv2(ctx).concat("/browser");
			PreyConfig.postUrl = null;
			
			preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, preyConfig);
		 
		 
			 
		} catch (Exception e) {
			PreyLogger.e("Contact wasn't send",e);
		} 
		return preyHttpResponse;
	}
	
	public PreyHttpResponse getContact(Context ctx){
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		PreyHttpResponse preyHttpResponse=null;
		try {
		HashMap<String, String> parameters=new HashMap<String, String> ();
		String url=getDeviceUrlApiv2(ctx).concat("/contacts.json");
		PreyLogger.d("url:"+url);
		preyHttpResponse=PreyRestHttpClient.getInstance(ctx).getAutentication2(url, parameters, preyConfig);
		} catch (Exception e) {
			PreyLogger.e("Contact wasn't send",e);
		} 
		 
		 return preyHttpResponse;
	}
	
	public PreyLocation getLocation(Context ctx,List<Wifi>listWifi) throws Exception{
		PreyLocation location=null;
		String url=googleLookup(listWifi);
		PreyLogger.d("location url:"+url);
		PreyHttpResponse response= PreyRestHttpClient.getInstance(ctx).getDefault(url);
		String responseAsString=response.getResponseAsString();
		PreyLogger.d("location resp:"+responseAsString);
		if (response.getStatusLine().getStatusCode()==200){
			if (responseAsString!=null&&responseAsString.indexOf("OK")>=0){
				location=new PreyLocation();
				JSONObject jsnobject = new JSONObject(response.getResponseAsString());
				String accuracy=jsnobject.getString("accuracy");
				JSONObject jsnobjectLocation = jsnobject.getJSONObject("location");
				String lat=jsnobjectLocation.getString("lat");
				String lng=jsnobjectLocation.getString("lng");
				location.setLat(Double.parseDouble(lat));
				location.setLng(Double.parseDouble(lng));
				location.setAccuracy(Float.parseFloat(accuracy));
			}
		}
		return location;
	}
	
	private String googleLookup(List<Wifi> listwifi){
		String queryString = "https://maps.googleapis.com/maps/api/browserlocation/json?browser=firefox&sensor=true";
		try {
			for(int i=0;listwifi!=null&&i<listwifi.size();i++){
				String ssid=listwifi.get(i).getSsid();
				ssid=ssid.replaceAll(" ", "%20");	
				queryString+="&wifi=mac:";
				queryString+=listwifi.get(i).getMacAddress();
				queryString+="%7C";
				queryString+="ssid:";
				queryString+=ssid;
				queryString+="%7C";
				queryString+="ss:";
				queryString+=listwifi.get(i).getSignalStrength();
				
			}	
		} catch (Exception e) {
		}
		return queryString;	
	}

}
