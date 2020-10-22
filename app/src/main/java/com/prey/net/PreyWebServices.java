/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.prey.FileConfigReader;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyName;
import com.prey.PreyPhone;
import com.prey.PreyPhone.Hardware;
import com.prey.PreyPhone.Wifi;
import com.prey.PreyUtils;
import com.prey.PreyVerify;
import com.prey.actions.HttpDataService;
import com.prey.actions.fileretrieval.FileretrievalDto;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.observer.ActionsController;
import com.prey.backwardcompatibility.AboveCupcakeSupport;
import com.prey.events.Event;
import com.prey.exceptions.PreyException;
import com.prey.json.parser.JSONParser;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.http.EntityFile;
import com.prey.R;

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
     */
    public PreyAccountData registerNewAccount(Context ctx, String name, String email, String password,String rule_age,String privacy_terms, String offer,String deviceType) throws Exception {
        return registerNewAccount(ctx,name,  email,  password,password, rule_age, privacy_terms, offer, deviceType);
    }

    public PreyAccountData registerNewAccount(Context ctx, String name, String email, String password1,String password2,String rule_age,String privacy_terms,String offers, String deviceType) throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password1);
        parameters.put("password_confirmation", password2);
        parameters.put("country_name", Locale.getDefault().getDisplayCountry());
        parameters.put("policy_rule_age", rule_age);
        parameters.put("policy_rule_privacy_terms", privacy_terms);
        parameters.put("mkt_newsletter", offers);
        parameters.put("lang", Locale.getDefault().getLanguage());

        PreyHttpResponse response = null;
        String xml = "";
        try {
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("signup.json");
            PreyLogger.d("url:"+url);
            response = PreyRestHttpClient.getInstance(ctx).post(url, parameters);
            if(response!=null) {
                xml = response.getResponseAsString();
                PreyLogger.d("code:" + response.getStatusCode() + " xml:" + xml);
            }else{
                PreyLogger.d("response nulo");
            }

        } catch (Exception e) {
            PreyLogger.e("error: "+e.getMessage(),e);
            throw new PreyException("{\"error\":[\""+ctx.getText(R.string.error_communication_exception).toString()+"\"]}" );
        }

        String apiKey = "";
        if (xml.contains("\"key\"")) {
            try {
                JSONObject jsnobject = new JSONObject(xml);
                apiKey = jsnobject.getString("key");
            } catch (Exception e) {

            }
        } else {

            if (response != null && response.getStatusCode() > 299) {
                PreyLogger.d("response.getStatusCode() >299 :"+response.getStatusCode());
                throw new PreyException(xml);
            }
        }
        String deviceId = null;
        PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType, PreyUtils.getNameDevice(ctx));
        if(responseDevice!=null){
            String xmlDeviceId = responseDevice.getResponseAsString();
            if (xmlDeviceId.contains("{\"key\"")) {
                try {
                    JSONObject jsnobject = new JSONObject(xmlDeviceId);
                    deviceId = jsnobject.getString("key");
                } catch (Exception e) {

                }
            } else {
                throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, ""));
            }
        }else {
            throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, ""));
        }

        PreyAccountData newAccount = new PreyAccountData();
        newAccount.setApiKey(apiKey);
        newAccount.setDeviceId(deviceId);
        newAccount.setEmail(email);
        newAccount.setPassword(password1);
        newAccount.setName(name);
        return newAccount;
    }

    /**
     * Register a new device for a given API_KEY, needed just after obtain the
     * new API_KEY.
     *
     * @throws PreyException
     */
    private PreyHttpResponse registerNewDevice(Context ctx, String api_key, String deviceType, String name) throws Exception {
        if(name==null||"".equals(name)){
            name = PreyUtils.getNameDevice(ctx);
        }
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String model = Build.MODEL;
        String vendor = "Google";
        try {
            vendor = AboveCupcakeSupport.getDeviceVendor();
        } catch (Exception e) {
        }
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("api_key", api_key);
        parameters.put("title", name);
        parameters.put("device_type", deviceType);
        parameters.put("os", "Android");
        parameters.put("os_version", Build.VERSION.RELEASE);
        parameters.put("referer_device_id", "");
        parameters.put("plan", "free");
        parameters.put("model_name", model);
        parameters.put("vendor_name", vendor);

        parameters = increaseData(ctx, parameters);

        String imei = new PreyPhone(ctx).getHardware().getAndroidDeviceId();
        parameters.put("physical_address", imei);
        String lang=Locale.getDefault().getLanguage();
        parameters.put("lang",lang);

        PreyHttpResponse response = null;
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices.json");
            PreyLogger.d("url:" + url);
            response = PreyRestHttpClient.getInstance(ctx).post(url, parameters);
            if (response == null) {
                throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, "[" + -1 + "]"));
            } else {
                PreyLogger.d("response:" + response.getStatusCode() + " " + response.getResponseAsString());
                String json = response.getResponseAsString();
                PreyLogger.d("json:" + json);
                if (response.getStatusCode() > 299) {
                    if("es".equals(lang))
                        throw new PreyException("{\"error\":[\"No queda espacio disponible para agregar este dispositivo!\"]}" );
                    else
                        throw new PreyException("{\"error\":[\"No slots left for new devices\"]}" );
                }
            }
        return response;
    }

    public PreyAccountData registerNewDeviceToAccount(Context ctx, String email, String password, String deviceType) throws Exception {
        PreyLogger.d("registerNewDeviceToAccount email:" + email + " password:" + password);
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        HashMap<String, String> parameters = new HashMap<String, String>();
        PreyHttpResponse response = null;
        String json;
        try {
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String lang = Locale.getDefault().getLanguage();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("profile.json?lang=").concat(lang);
            PreyLogger.d("_____url:" + url);
            response = PreyRestHttpClient.getInstance(ctx).get(url, parameters, email, password);
            PreyLogger.d("response:" + response);
            json = response.getResponseAsString();
            PreyLogger.d("json:" + json);
        } catch (Exception e) {
            PreyLogger.e("Error!"+e.getMessage(), e);
            throw new PreyException("{\"error\":[\""+ctx.getText(R.string.error_communication_exception).toString()+"\"]}" );
        }
        String status = "";
        if (response != null  ) {
            status = "[" + response.getStatusCode() + "]";
        }
        if (!json.contains("key")) {
            PreyLogger.d("no key");
            throw new PreyException(json );
        }

        int from;
        int to;
        String apiKey=null;
        try {
            JSONObject jsonObject=new JSONObject(json);
            apiKey = jsonObject.getString("key");
            PreyLogger.d("apikey:" + apiKey);
        } catch (Exception e) {
            throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, status));
        }
        String deviceId = null;
        PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType, PreyUtils.getNameDevice(ctx));
        if(responseDevice!=null) {
            String xmlDeviceId = responseDevice.getResponseAsString();
            //if json
            if (xmlDeviceId.contains("key")) {
                try {
                    JSONObject jsnobject = new JSONObject(xmlDeviceId);
                    deviceId = jsnobject.getString("key");
                } catch (Exception e) {

                }
            }
        }else{
            throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, status));
        }
        PreyAccountData newAccount = new PreyAccountData();
        newAccount.setApiKey(apiKey);
        newAccount.setDeviceId(deviceId);
        newAccount.setEmail(email);
        newAccount.setPassword(password);
        return newAccount;

    }

    public PreyAccountData registerNewDeviceWithApiKeyEmail(Context ctx, String apiKey, String email, String deviceType, String name) throws Exception {
        String deviceId = null;
        PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType,name);
        String xmlDeviceId = null;
        if(responseDevice!=null) {
            xmlDeviceId = responseDevice.getResponseAsString();
        }
        //if json
        if (xmlDeviceId!=null&&xmlDeviceId.contains("{\"key\"")) {
            try {
                JSONObject jsnobject = new JSONObject(xmlDeviceId);
                deviceId = jsnobject.getString("key");
            } catch (Exception e) {
            }
        }
        PreyAccountData newAccount =null;
        if (deviceId!=null&&!"".equals(deviceId)) {
            newAccount = new PreyAccountData();
            newAccount.setApiKey(apiKey);
            newAccount.setDeviceId(deviceId);
            newAccount.setEmail(email);
            newAccount.setPassword("");
        }
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
        PreyHttpResponse response = PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
        if (response != null ) {
            int code=response.getStatusCode();
            PreyLogger.d("setPushRegistrationId code:"+code);
            if ( code == HttpURLConnection.HTTP_OK ) {
                PreyLogger.d("setPushRegistrationId c2dm registry id set succesfully");
            }
        }
        return response;
    }

    public boolean checkPassword(Context ctx, String apikey, String password) throws PreyException {
        PreyHttpResponse response= this.checkPassword(apikey, password, ctx);
        if(response!=null) {
            String xml = response.getResponseAsString();
            if(xml!=null) {
                return xml.contains("key");
            }
        }
        return false;
    }

    public boolean checkPassword2(Context ctx, String apikey, String password, String password2) throws PreyException {

        PreyLogger.d("checkPassword2 password:"+password+" password2:"+password2);

        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        String url=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("authenticate");
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("email",PreyConfig.getPreyConfig(ctx).getEmail());
        parameters.put("password",password);
        parameters.put("otp_code",password2);
        parameters.put("lang",Locale.getDefault().getLanguage());
        PreyHttpResponse response=null;
        try {
            response=PreyRestHttpClient.getInstance(ctx).postAutentication(url,parameters);
        }catch (Exception e){}
        if(response!=null){
            PreyLogger.d("authenticate:" + response.getResponseAsString());
            if(response.getStatusCode()==HttpURLConnection.HTTP_OK) {
                String tokenJwt ="";
                try {
                        JSONObject jsnobject = new JSONObject(response.getResponseAsString());
                        tokenJwt = jsnobject.getString("token");
                        PreyConfig.getPreyConfig(ctx).setTokenJwt(tokenJwt);
                }catch (Exception e){}
                return true;
            }else{
                String json="";
                try {
                    JSONObject jsnobject = new JSONObject(response.getResponseAsString());
                    json = response.getResponseAsString();
                }catch (Exception e){}
                try {
                    JSONObject jsnobject = new JSONObject(response.getResponseAsString());
                    JSONArray array=jsnobject.getJSONArray("error");
                    String json2="";
                    for(int i=0;array!=null&&i<array.length();i++){
                        json2+=array.get(i);
                        if((i+1)<array.length()){
                            json2+=" ,";
                        }
                    }
                    json=json2;
                } catch (Exception e) {
                    PreyLogger.e("error:"+e.getMessage(),e);
                }
                throw new PreyException(json+ " [" + response.getStatusCode() + "]" );
            }
        }else {
            throw new PreyException(ctx.getText(R.string.password_wrong).toString());
        }
    }

    private PreyHttpResponse checkPassword(String apikey, String password, Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        HashMap<String, String> parameters = new HashMap<String, String>();
        PreyHttpResponse response=null;
        String json="";
        try {
            String uri=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("api/v2/profile.json?lang="+Locale.getDefault().getLanguage());
            response = PreyRestHttpClient.getInstance(ctx).get(uri, parameters, apikey, password);
            json = response.getResponseAsString();
        } catch (Exception e) {
            response = null;
            String err = "" + ctx.getText(R.string.error_communication_exception);
            json = "{\"error\":[\"" + err + "\"]}";
        }
        if(response == null){
            throw new PreyException(json);
        }
        if(response!=null&&response.getStatusCode()== HttpURLConnection.HTTP_UNAUTHORIZED){
            throw new PreyException(json);
        }
        try {
            PreyLogger.d("____[token]_________________apikey:"+apikey+" password:"+password);
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String uri2=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("get_token.json");
            PreyHttpResponse response2 = PreyRestHttpClient.getInstance(ctx).get(uri2, parameters, apikey, password,"application/json");
            if(response2!=null) {
                PreyLogger.d("get_token:" + response2.getResponseAsString());
                JSONObject jsnobject = new JSONObject(response2.getResponseAsString());
                String tokenJwt = jsnobject.getString("token");
                PreyLogger.d("tokenJwt:" + tokenJwt);
                PreyConfig.getPreyConfig(ctx).setTokenJwt(tokenJwt);
            }else{
                PreyLogger.d("token: nulo");
            }

        } catch (Exception e) {
            PreyLogger.e("error:"+e.getMessage(),e);

        }
        return response;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = android.util.Base64.decode(strEncoded, android.util.Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }


    public String deleteDevice(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        HashMap<String, String> parameters = new HashMap<String, String>();
        String xml=null;
        try {
            String url = this.getDeviceWebControlPanelUiUrl(ctx);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx)
                    .delete(url, parameters);
            if(response!=null) {
                PreyLogger.d(response.toString());
                xml = response.getResponseAsString();
            }
        } catch (Exception e) {
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
            if (response.getStatusCode() != 302) {
                throw new PreyException(ctx.getText(R.string.error_cant_report_forgotten_password).toString());
            }
        } catch (Exception e) {
            throw new PreyException(ctx.getText(R.string.error_cant_report_forgotten_password).toString(), e);
        }

        return true;
    }

    public static String getDeviceWebControlPanelUrl(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String deviceKey = preyConfig.getDeviceId();
        if (deviceKey == null || deviceKey == "")
            throw new PreyException("Device key not found on the configuration");
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        //apiv2="";
        return PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
    }

    public String getDeviceWebControlPanelUiUrl(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String deviceKey = preyConfig.getDeviceId();
        if (deviceKey == null || deviceKey == "")
            throw new PreyException("Device key not found on the configuration");
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        return PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
    }


    private String getDeviceUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat(".json");
    }

    private String getReportUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/reports.json");
    }

    public String getFileUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/files.json");
    }

    public String getDataUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/data.json");
    }

    private String getEventsUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/events");
    }

    private String getResponseUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/response");
    }

    public String getInfoUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/info.json");
    }

    public String getLocationUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/location.json");
    }

    public String getMissing(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/missing");
    }

    private String getDeviceUrlApiv2(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String deviceKey = preyConfig.getDeviceId();
        if (deviceKey == null || deviceKey == "")
            throw new PreyException("Device key not found on the configuration");
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
        return url;
    }

    public String getDeviceUrlV2(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String deviceKey = preyConfig.getDeviceId();
        if (deviceKey == null || deviceKey == "")
            throw new PreyException("Device key not found on the configuration");
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey);
        return url;
    }

    public String getDeviceUrl(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String deviceKey = preyConfig.getDeviceId();
        if (deviceKey == null || deviceKey == "")
            throw new PreyException("Device key not found on the configuration");

        String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("devices/").concat(deviceKey);
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
        parameters.put(prefix + "[ram_size]", "" + hardware.getTotalMemory());
        parameters.put(prefix + "[serial_number]", hardware.getSerialNumber());
        // parameters.put(prefix + "[ram_modules]", hardware.getRamModules());
        int nic = 0;
        Wifi wifi = phone.getWifi();
        if (wifi != null) {
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
        List<EntityFile> entityFiles = new ArrayList<EntityFile>();
        for (HttpDataService httpDataService : dataToSend) {
            if (httpDataService != null) {
                parameters.putAll(httpDataService.getDataAsParameters());
                if (httpDataService.getEntityFiles() != null && httpDataService.getEntityFiles().size() > 0) {
                    entityFiles.addAll(httpDataService.getEntityFiles());
                }
            }
        }
        PreyHttpResponse preyHttpResponse = null;
        if(parameters.size()>0||entityFiles.size()>0) {
            Hardware hardware = new PreyPhone(ctx).getHardware();
            if (!PreyConfig.getPreyConfig(ctx).isSendData() && hardware.getTotalMemory() > 0) {
                PreyConfig.getPreyConfig(ctx).setSendData(true);
                parameters.put("hardware_attributes[ram_size]", "" + hardware.getTotalMemory());
            }
            if (!"".equals(hardware.getUuid()) && !PreyConfig.getPreyConfig(ctx).isSentUuidSerialNumber()) {
                parameters.put("hardware_attributes[uuid]", hardware.getUuid());
                parameters.put("hardware_attributes[serial_number]", hardware.getSerialNumber());
                PreyConfig.getPreyConfig(ctx).setSentUuidSerialNumber(true);
            }
            try {
                String url = getDataUrlJson(ctx);
                PreyLogger.d("URL:" + url);
                PreyConfig.postUrl = null;
                if(UtilConnection.isInternetAvailable(ctx)) {
                    if (entityFiles.size() == 0) {
                        preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters);
                    } else {
                        preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, entityFiles);
                    }
                    PreyLogger.d("Data sent_: " + (preyHttpResponse==null?"":preyHttpResponse.getResponseAsString()));
                }
            } catch (Exception e) {
                PreyLogger.e("Data wasn't send", e);
            }
        }
        return preyHttpResponse;
    }

    public PreyVerify verifyUsers(Context ctx) throws Exception {
        String apiKey= PreyConfig.getPreyConfig(ctx).getApiKey();
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("users/verify.json");;
        PreyHttpResponse preyHttpResponse = null;
        preyHttpResponse = PreyRestHttpClient.getInstance(ctx).getAutentication(url,null);
        PreyVerify verify=null;
        if(preyHttpResponse!=null) {
            String body = preyHttpResponse.getResponseAsString();
            if (body != null)
                body = body.trim();
            int statusCode=preyHttpResponse.getStatusCode();
            PreyLogger.d("verify code:"+statusCode);
            PreyLogger.d("verify body:"+body);
            verify=new PreyVerify();
            verify.setStatusCode(statusCode);
            verify.setStatusDescription(body);
        }
        return verify;
    }

    public PreyHttpResponse sendPreyHttpEvent(Context ctx, Event event, JSONObject jsonObject) {
        PreyHttpResponse preyHttpResponse = null;
        try {
            String url = getEventsUrlJson(ctx);
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("name", event.getName());
            parameters.put("info", event.getInfo());
            parameters.put("status", jsonObject.toString());
            PreyLogger.d("EVENT sendPreyHttpEvent url:" + url);
            PreyLogger.d("EVENT name:" + event.getName() + " info:" + event.getInfo());
            PreyLogger.d("EVENT status:" + jsonObject.toString());
            String status = jsonObject.toString();
            preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postStatusAutentication(url, status, parameters);
            if(preyHttpResponse!=null) {
                String jsonString = preyHttpResponse.getResponseAsString();
                if (jsonString != null && jsonString.length() > 0) {
                    List<JSONObject> jsonObjectList = new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
                    if (jsonObjectList != null && jsonObjectList.size() > 0) {
                        ActionsController.getInstance(ctx).runActionJson(ctx, jsonObjectList);
                    }
                }
            }
        } catch (Exception e) {
            PreyLogger.e("Event wasn't send", e);
        }
        return preyHttpResponse;
    }

    public String sendNotifyActionResultPreyHttp(Context ctx, Map<String, String> params) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String response = null;
        try {
            String url = getResponseUrlJson(ctx);
            PreyConfig.postUrl = null;
            PreyHttpResponse httpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, params);
            response = httpResponse.toString();
        } catch (Exception e) {
            //PreyLogger.e("Notify Action Result wasn't send",e);
        }
        return response;
    }

    public void sendNotifyActionResultPreyHttp(Context ctx, String correlationId, Map<String, String> params) {
        sendNotifyActionResultPreyHttp(ctx,null,correlationId,params);
    }
    public void sendNotifyActionResultPreyHttp(final Context ctx,final String status,final String correlationId,final Map<String, String> params) {

        new Thread() {
            public void run() {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String response = null;
        try {
            String url = getResponseUrlJson(ctx);
            PreyConfig.postUrl = null;
            PreyHttpResponse httpResponse = PreyRestHttpClient.getInstance(ctx).postAutenticationCorrelationId(url, status,correlationId,params);
            response = httpResponse.toString();
        } catch (Exception e) {
            //PreyLogger.e("Notify Action Result wasn't send",e);
        }
            }
        }.start();

    }

    public PreyHttpResponse sendPreyHttpReport(Context ctx, List<HttpDataService> dataToSend) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);

        HashMap<String, String> parameters = new HashMap<String, String>();
        List<EntityFile> entityFiles = new ArrayList<EntityFile>();
        for (HttpDataService httpDataService : dataToSend) {
            if (httpDataService != null) {
                parameters.putAll(httpDataService.getReportAsParameters());
                if (httpDataService.getEntityFiles() != null && httpDataService.getEntityFiles().size() > 0) {
                    entityFiles.addAll(httpDataService.getEntityFiles());
                }
            }
        }

        PreyHttpResponse preyHttpResponse = null;
        try {
            String url = getReportUrlJson(ctx);
            PreyConfig.postUrl = null;
            PreyLogger.d("report url:" + url);


            if (entityFiles == null || entityFiles.size() == 0)
                preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutenticationTimeout(url, parameters);
            else
                preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, entityFiles);
            PreyLogger.d("Report sent: " + (preyHttpResponse==null?"":preyHttpResponse.getResponseAsString()));
        } catch (Exception e) {
            PreyLogger.e("Report wasn't send:" + e.getMessage(), e);
        }
        return preyHttpResponse;
    }

    public List<JSONObject> getActionsJsonToPerform(Context ctx) throws PreyException {
        String url = getDeviceUrlJson(ctx);
        List<JSONObject> lista = new JSONParser().getJSONFromUrl(ctx, url);
        return lista;
    }

    public PreyHttpResponse sendContact(Context ctx, HashMap<String, String> parameters) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);


        PreyHttpResponse preyHttpResponse = null;
        try {

            String url = getDeviceUrlApiv2(ctx).concat("/contacts");

            PreyConfig.postUrl = null;


            preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters);


        } catch (Exception e) {
            PreyLogger.e("Contact wasn't send", e);
        }
        return preyHttpResponse;
    }

    public PreyHttpResponse sendLocation(Context ctx,JSONObject jsonParam) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        PreyHttpResponse preyHttpResponse = null;
        try {
            String url = getLocationUrlJson(ctx);
            if(UtilConnection.isInternetAvailable(ctx)) {
                preyHttpResponse = PreyRestHttpClient.getInstance(ctx).jsonMethodAutentication(url,UtilConnection.REQUEST_METHOD_POST,jsonParam);
            }
        } catch (Exception e) {
            PreyLogger.e("Contact wasn't send", e);
        }
        return preyHttpResponse;
    }

    public PreyHttpResponse sendBrowser(Context ctx, HashMap<String, String> parameters) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        PreyHttpResponse preyHttpResponse = null;
        try {
            String url = getDeviceUrlApiv2(ctx).concat("/browser");
            PreyConfig.postUrl = null;

            preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters);


        } catch (Exception e) {
            PreyLogger.e("Contact wasn't send", e);
        }
        return preyHttpResponse;
    }

    public PreyHttpResponse getContact(Context ctx) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        PreyHttpResponse preyHttpResponse = null;
        try {
            HashMap<String, String> parameters = new HashMap<String, String>();
            String url = getDeviceUrlApiv2(ctx).concat("/contacts.json");
            PreyLogger.d("url:" + url);
            preyHttpResponse = PreyRestHttpClient.getInstance(ctx).getAutentication(url, parameters);
        } catch (Exception e) {
            PreyLogger.e("Contact wasn't send", e);
        }

        return preyHttpResponse;
    }

    public String getIPAddress(Context ctx)throws Exception {
        String uri="http://ifconfig.me/ip";
        PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).get(uri,null);
        String responseAsString = "";
        if(response!=null) {
            responseAsString = response.getResponseAsString();
            PreyLogger.d("responseAsString:" + responseAsString);
        }
        return responseAsString;
    }

    public String geofencing(Context ctx) throws PreyException {
        String url = getDeviceUrlApiv2(ctx).concat("/geofencing.json");
        PreyLogger.d("url:"+url);
        String sb=null;
        PreyRestHttpClient preyRestHttpClient=PreyRestHttpClient.getInstance(ctx);
        try{
            Map<String, String> params=null;
            PreyHttpResponse response=PreyRestHttpClient.getInstance(ctx).getAutentication(url, params);
            if(response!=null) {
                if(response.getStatusCode()==HttpURLConnection.HTTP_OK) {
                    sb = response.getResponseAsString();
                    if (sb != null)
                        sb = sb.trim();
                }
            }
        }catch(Exception e){
            PreyLogger.e("Error, causa:" + e.getMessage(), e);
            return null;
        }
        return sb;
    }


    public JSONObject getStatus(Context ctx) throws PreyException {
        JSONObject jsnobject =null;
        String url = getDeviceUrlApiv2(ctx).concat("/status.json");
        PreyLogger.d("getStatus url:"+url);
        PreyHttpResponse response=null;
        PreyRestHttpClient preyRestHttpClient=PreyRestHttpClient.getInstance(ctx);
        try{
            Map<String, String> params=null;
            response=PreyRestHttpClient.getInstance(ctx).getAutentication(url, params);
            if (response !=null){
                if(response.getStatusCode()==HttpURLConnection.HTTP_OK) {
                    String responseAsString = response.getResponseAsString();
                    if (responseAsString != null) {
                        jsnobject = new JSONObject(response.getResponseAsString());
                    }
                }
            }
        }catch(Exception e){
            PreyLogger.e("Error, causa:" + e.getMessage(), e);
            return null;
        }
        return jsnobject;
    }

    public void sendEvent(final Context ctx,final int id  ) {
    }

    public PreyHttpResponse sendTree(final Context ctx,JSONObject json  ) throws PreyException{
        String uri = getDeviceUrlApiv2(ctx).concat("/data.json");
        return PreyRestHttpClient.getInstance(ctx).jsonMethodAutentication(uri,UtilConnection.REQUEST_METHOD_POST,json);
    }

    public int uploadFile(Context ctx, File file,String uploadID,long total)  throws PreyException{
        String uri = PreyConfig.getPreyConfig(ctx).getPreyUrl() + "upload/upload?uploadID=" + uploadID;
        return PreyRestHttpClient.getInstance(ctx).uploadFile(ctx,uri,file,total);
    }

    public FileretrievalDto uploadStatus(Context ctx,String uploadID)  throws Exception {
        FileretrievalDto dto=null;
        String uri = PreyConfig.getPreyConfig(ctx).getPreyUrl() + "upload/upload?uploadID=" + uploadID;
        PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).get(uri,null);
        if(response!=null) {
            String responseAsString = response.getResponseAsString();
            PreyLogger.d("uploadStatus resp:" + responseAsString);
            if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                if (responseAsString != null) {
                    JSONObject jsnobject = new JSONObject(response.getResponseAsString());
                    String id = jsnobject.getString("ID");
                    String name = jsnobject.getString("Name");
                    String size = jsnobject.getString("Size");
                    String total = jsnobject.getString("Total");
                    String status = jsnobject.getString("Status");
                    String path = jsnobject.getString("Path");
                    dto = new FileretrievalDto();
                    dto.setFileId(id);
                    dto.setName(name);
                    dto.setSize(Long.parseLong(size));
                    dto.setTotal(Long.parseLong(total));
                    dto.setStatus(Integer.parseInt(status));
                    dto.setPath(path);
                }
            }
            if (response.getStatusCode() == 404) {
                dto = new FileretrievalDto();
                dto.setStatus(response.getStatusCode());
            }
        }
        return dto;
    }

    public String googlePlayVersion(Context ctx) {
        try {
            String uri = PreyConfig.getPreyConfig(ctx).getPreyGooglePlay();
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).get(uri, null);
            if(response!=null) {
                String responseAsString = response.getResponseAsString();
                int po = responseAsString.indexOf("softwareVersion\">");
                responseAsString = responseAsString.substring(po + 17);
                po = responseAsString.indexOf("</");
                responseAsString = responseAsString.substring(0, po);
                return responseAsString.trim();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String getUuidDevice(Context ctx){
        String uuid = null;
        try {
            String uri = getInfoUrlJson(ctx);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).getAutentication(uri, null);
            if(response!=null) {
                String out = response.getResponseAsString();
                JSONObject jsnobject = new JSONObject(out);
                uuid = jsnobject.getString("uuid");
            }
        } catch (Exception e) {
        }
        return uuid;
    }

    public String getNameDevice(Context ctx){
        String name = null;
        try {
            String uri = getInfoUrlJson(ctx);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).getAutentication(uri, null);
            if(response!=null) {
                String out = response.getResponseAsString();
                PreyLogger.d("getNameDevice:"+out);
                JSONObject jsnobject = new JSONObject(out);
                name = jsnobject.getString("name");
            }
        } catch (Exception e) {
        }
        return name;
    }

    public String getEmail(Context ctx) {
        String email = null;
        try {
            HashMap<String, String> parameters = new HashMap<String, String>();
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("profile.json");
            PreyLogger.d("url:" + url);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).getAutentication(url, parameters);
            if(response!=null) {
                String out = response.getResponseAsString();
                PreyLogger.d("out:" + out);
                JSONObject jsnobject = new JSONObject(out);
                email = jsnobject.getString("email");
                PreyLogger.d("email:" + email);
            }
        } catch (Exception e) {
            PreyLogger.e("error get email", e);
        }
        return email;
    }

    public boolean getTwoStepEnabled(Context ctx) {
        boolean TwoStepEnabled = false;
        try {
            HashMap<String, String> parameters = new HashMap<String, String>();

            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("profile?api_key="+PreyConfig.getPreyConfig(ctx).getApiKey());
            PreyLogger.d("url:" + url);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).getAutentication(url, parameters);
            if(response!=null) {
                String out = response.getResponseAsString();
                PreyLogger.d("out:" + out);
                JSONObject jsnobject = new JSONObject(out);
                TwoStepEnabled = jsnobject.getBoolean("two_step_enabled?");
                PreyLogger.d("TwoStepEnabled:" + TwoStepEnabled);
            }
        } catch (Exception e) {
            PreyLogger.e("error get TwoStepEnabled", e);
        }
        return TwoStepEnabled;
    }


    public PreyHttpResponse setMissing(final Context ctx) throws Exception{
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        String deviceId = PreyConfig.getPreyConfig(ctx).getDeviceId();
        String uri =PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("set-missing/").concat(deviceId).concat("/missing");
        Map<String, String> params=new HashMap<>();
        return PreyRestHttpClient.getInstance(ctx).postAutentication(uri,params);
    }

    public String triggers(Context ctx) throws PreyException {
        String url =  getDeviceUrlApiv2(ctx).concat("/triggers.json");
        PreyLogger.d("url:"+url);
        String sb=null;
        try{
            Map<String, String> params=null;
            PreyHttpResponse response=PreyRestHttpClient.getInstance(ctx).getAutentication(url, params);
            if(response!=null) {
                if(response.getStatusCode()==HttpURLConnection.HTTP_OK) {
                    sb = response.getResponseAsString();
                    if (sb != null)
                        sb = sb.trim();
                }
            }
        }catch(Exception e){
            PreyLogger.e("Error, causa:" + e.getMessage(), e);
            return null;
        }
        return sb;
    }

    public PreyHttpResponse setMissing(final Context ctx,String estado) throws Exception{
        String uri =getMissing(ctx).concat("/").concat(estado);
        Map<String, String> params=new HashMap<>();
        return PreyRestHttpClient.getInstance(ctx).postAutentication(uri,params);
    }

    public PreyLocation geolocate(final Context ctx){
        PreyLocation location=null;
        try{
            String url=PreyConfig.getPreyConfig(ctx).getPreyUrl()+"geo";
            JSONObject jsonParam=new JSONObject();
            JSONArray array=new JSONArray();
            if (PreyConnectivityManager.getInstance(ctx).isWifiConnected()) {
                HashMap<String, String> parametersMapWifi = new HashMap<String, String>();
                PreyPhone preyPhone = new PreyPhone(ctx);
                List<Wifi> listWifi = preyPhone.getListWifi();
                for (int i = 0; listWifi != null && i < listWifi.size()&& i<15; i++) {
                    Wifi wifi = listWifi.get(i);
                    JSONObject jsonRed=new JSONObject();
                    jsonRed.put("macAddress", wifi.getMacAddress());
                    jsonRed.put("ssid",  wifi.getSsid());
                    jsonRed.put("signalStrength", Integer.parseInt(wifi.getSignalStrength()));
                    jsonRed.put( "channel", Integer.parseInt(wifi.getChannel()));
                    PreyLogger.d("GEO "+i+" out:" + jsonRed.toString());
                    array.put(jsonRed);
                }
            }
            jsonParam.put("wifiAccessPoints",array);
            PreyHttpResponse response=PreyRestHttpClient.getInstance(ctx).jsonMethodAutentication(url,UtilConnection.REQUEST_METHOD_POST,jsonParam);
            if(response!=null) {
                if(response.getStatusCode()==HttpURLConnection.HTTP_OK){
                    String out = response.getResponseAsString();
                    JSONObject outJson=new JSONObject(out);
                    if(!outJson.isNull("geolocation")) {
                        JSONObject geolocationJson = outJson.getJSONObject("geolocation");
                        JSONObject locationJSon=geolocationJson.getJSONObject("location");
                        double lat=locationJSon.getDouble("lat");
                        double lng=locationJSon.getDouble("lng");
                        int accuracy=geolocationJson.getInt("accuracy");
                        location=new PreyLocation();
                        location.setLat(lat);
                        location.setLng(lng);
                        location.setAccuracy(accuracy);
                        location.setMethod("wifi");
                    }
                    if(!outJson.isNull("endpoint")) {
                        JSONObject endpointJson=outJson.getJSONObject("endpoint");
                        String urlJson=endpointJson.getString("url");
                        String userAgentJson=endpointJson.getString("user-agent");
                        PreyHttpResponse response2=UtilConnection.postJson(urlJson,userAgentJson,jsonParam);
                        if(response2.getStatusCode()==HttpURLConnection.HTTP_OK){
                            String out2 = response2.getResponseAsString();
                            JSONObject outJson2=new JSONObject(out2);
                            JSONObject locationJSon2=outJson2.getJSONObject("location");
                            double lat=locationJSon2.getDouble("lat");
                            double lng=locationJSon2.getDouble("lng");
                            int accuracy=outJson2.getInt("accuracy");
                            location=new PreyLocation();
                            location.setLat(lat);
                            location.setLng(lng);
                            location.setAccuracy(accuracy);
                            location.setMethod("wifi");
                        }
                    }
                }
            }
        }catch(Exception e){
            PreyLogger.d("error geolocate:" + e.getMessage());
        }
        return location;
    }

    public PreyVerify verifyEmail(Context ctx,String email) throws Exception {
        String apiKey= PreyConfig.getPreyConfig(ctx).getApiKey();
        String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
        String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("users/verify_email.json");
        PreyHttpResponse preyHttpResponse = null;
        JSONObject jsonParam=new  JSONObject ();
        jsonParam.put("email",email);
        jsonParam.put("lang", Locale.getDefault().getLanguage());
        preyHttpResponse = PreyRestHttpClient.getInstance(ctx).jsonMethodAutentication(url,UtilConnection.REQUEST_METHOD_PUT,jsonParam);
        PreyVerify verify=null;
        if(preyHttpResponse!=null) {
            String body = preyHttpResponse.getResponseAsString();
            if (body != null)
                body = body.trim();
            int statusCode=preyHttpResponse.getStatusCode();
            PreyLogger.d("verify code:"+statusCode);
            PreyLogger.d("verify body:"+body);
            verify=new PreyVerify();
            verify.setStatusCode(statusCode);
            verify.setStatusDescription(body);
        }
        return verify;
    }

    public PreyName validateName(final Context ctx,String name){
        PreyName preyName=new PreyName();
        try{
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            PreyConfig config=PreyConfig.getPreyConfig(ctx);
            String deviceKey = config.getDeviceId();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey).concat("/validate.json");
            PreyLogger.d("validateName name:"+name);
            JSONObject jsonParam=new JSONObject();
            jsonParam.put("name", name);
            jsonParam.put("lang", Locale.getDefault().getLanguage());
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).jsonMethodAutentication(url,UtilConnection.REQUEST_METHOD_POST,jsonParam);
            PreyLogger.d("validateName getStatusCode:"+response.getStatusCode());
            preyName.setCode(response.getStatusCode());
        }catch(Exception e){
            PreyLogger.d("validateName error validate:" + e.getMessage());
        }
        return preyName;
    }

    public PreyName renameName(final Context ctx, String name){
        PreyName preyName=new PreyName();
        try{
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            PreyConfig config=PreyConfig.getPreyConfig(ctx);
            String deviceKey = config.getDeviceId();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices/").concat(deviceKey).concat(".json");
            JSONObject jsonParam=new JSONObject();
            jsonParam.put("name", name);
            jsonParam.put("lang", Locale.getDefault().getLanguage());
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).jsonMethodAutentication(url,UtilConnection.REQUEST_METHOD_PUT,jsonParam);
            PreyLogger.d("renameName:"+response.getStatusCode());
            preyName.setCode(response.getStatusCode());
            if(response.getStatusCode()==HttpURLConnection.HTTP_OK) {
                String out = response.getResponseAsString();
                PreyLogger.d("renameName:"+out);
            }
            if(response.getStatusCode()==422) {
                String out = response.getResponseAsString();
                PreyLogger.d("renameName:"+out);
                JSONObject outJson = new JSONObject(out);
                String name_available_error="";
                String name_available="";
                if(out.indexOf("\"title\"")>0){
                    JSONArray array2 = outJson.getJSONArray("title");
                    for (int i = 0; array2 != null && i < array2.length(); i++) {
                        try {
                            String outJson1 = (String) array2.getString(i);
                            if ("".equals(name_available_error)) {
                                String s = outJson1.substring(0, 1).toUpperCase();
                                name_available_error = s + outJson1.substring(1);
                            } else {
                                name_available_error += ", " + outJson1;
                            }
                        }catch (Exception e){
                            name_available_error=e.getMessage();
                        }
                    }
                }else {
                    JSONArray array1 = outJson.getJSONArray("name_available_error");
                    for (int i = 0; array1 != null && i < array1.length(); i++) {
                        try {
                            String outJson1 = (String) array1.getString(i);
                            if ("".equals(name_available_error)) {
                                name_available_error = outJson1;
                            } else {
                                name_available_error += ", " + outJson1;
                            }
                        }catch (Exception e){
                            name_available_error=e.getMessage();
                        }
                    }
                    JSONArray array2 = outJson.getJSONArray("name_available");
                    for (int i = 0; array2 != null && i < array2.length(); i++) {
                        try {
                            String outJson2 = (String) array2.getString(i);
                            if ("".equals(name_available)) {
                                name_available = outJson2;
                            } else {
                                name_available += ", " + outJson2;
                            }
                        }catch (Exception e){
                            name_available_error=e.getMessage();
                        }
                    }
                }
                preyName.setError(name_available_error);
                preyName.setName(name_available);
            }
        }catch(Exception e){
            PreyLogger.d("error validate:" + e.getMessage());
        }
        return preyName;
    }

}