/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



import org.json.JSONArray;
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
import com.prey.actions.fileretrieval.FileretrievalDto;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.observer.ActionsController;
import com.prey.backwardcompatibility.AboveCupcakeSupport;
import com.prey.events.Event;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.json.parser.JSONParser;
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
    public PreyAccountData registerNewAccount(Context ctx, String name, String email, String password, String deviceType) throws PreyException {


        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("password_confirmation", password);
        parameters.put("country_name", Locale.getDefault().getDisplayCountry());


        PreyHttpResponse response = null;
        String xml = "";
        try {
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("signup.json");

            response = PreyRestHttpClient.getInstance(ctx).post(url, parameters);
            xml = response.getResponseAsString();
        } catch (Exception e) {
            PreyLogger.e("error: "+e.getMessage(),e);
            throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
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
                if (response.getStatusCode() == 422 && xml.indexOf("already") > 0) {
                    throw new PreyException(ctx.getString(R.string.error_already_register));
                }
                throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, "[" + response.getStatusCode() + "]"));
            } else {
                throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, ""));
            }
        }

        PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType);
        String xmlDeviceId = responseDevice.getResponseAsString();
        String deviceId = null;
        if (xmlDeviceId.contains("{\"key\"")) {
            try {
                JSONObject jsnobject = new JSONObject(xmlDeviceId);
                deviceId = jsnobject.getString("key");
            } catch (Exception e) {

            }
        } else {
            throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, ""));
        }

        PreyAccountData newAccount = new PreyAccountData();
        newAccount.setApiKey(apiKey);
        newAccount.setDeviceId(deviceId);
        newAccount.setEmail(email);
        newAccount.setPassword(password);
        newAccount.setName(name);
        return newAccount;
    }


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
        try {
            vendor = AboveCupcakeSupport.getDeviceVendor();
        }catch(Exception e){
        }
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("api_key", api_key);
        parameters.put("title", vendor + " " + model);
        parameters.put("device_type", deviceType);
        parameters.put("os", "Android");
        parameters.put("os_version", Build.VERSION.RELEASE);
        parameters.put("referer_device_id", "");
        parameters.put("plan", "free");
        parameters.put("model_name", model);
        parameters.put("vendor_name", vendor);

        parameters = increaseData(ctx, parameters);
        TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        //String imsi = mTelephonyMgr.getSubscriberId();
        String imei = new PreyPhone(ctx).getHardware().getAndroidDeviceId();
        parameters.put("physical_address", imei);

        PreyHttpResponse response = null;
        try {
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("devices.json");
            PreyLogger.d("url:" + url);
            response = PreyRestHttpClient.getInstance(ctx).post(url, parameters);
            if (response == null) {
                throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, "[" + -1 + "]"));
            } else {
                PreyLogger.d("response:" + response.getStatusCode() + " " + response.getResponseAsString());
                // No more devices allowed

                if ((response.getStatusCode() == 302) || (response.getStatusCode() == 422) || (response.getStatusCode() == 403)) {
                    throw new NoMoreDevicesAllowedException(ctx.getText(R.string.set_old_user_no_more_devices_text).toString());
                }
                if (response.getStatusCode() > 299) {
                    throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, "[" + response.getStatusCode() + "]"));
                }
            }
        } catch (Exception e) {
            PreyLogger.e("error:"+e.getMessage(),e);
            throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
        }

        return response;
    }

    public PreyAccountData registerNewDeviceToAccount(Context ctx, String email, String password, String deviceType) throws PreyException {
        PreyLogger.d("ws email:" + email + " password:" + password);
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        HashMap<String, String> parameters = new HashMap<String, String>();
        PreyHttpResponse response = null;
        String xml;
        try {
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("profile.xml");
            PreyLogger.d("_____url:" + url);
            response = PreyRestHttpClient.getInstance(ctx).get(url, parameters, email, password);
            xml = response.getResponseAsString();
            PreyLogger.d("xml:" + xml);
        } catch (Exception e) {
            PreyLogger.e("Error!"+e.getMessage(), e);
            throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
        }
        String status = "";
        if (response != null  ) {
            status = "[" + response.getStatusCode() + "]";
        }
        if (!xml.contains("<key")) {
            throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, status));
        }

        int from;
        int to;
        String apiKey;
        try {
            from = xml.indexOf("<key>") + 5;
            to = xml.indexOf("</key>");
            apiKey = xml.substring(from, to);
        } catch (Exception e) {
            throw new PreyException(ctx.getString(R.string.error_cant_add_this_device, status));
        }
        String deviceId = null;
        PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType);
        String xmlDeviceId = responseDevice.getResponseAsString();
        //if json
        if (xmlDeviceId.contains("{\"key\"")) {
            try {
                JSONObject jsnobject = new JSONObject(xmlDeviceId);
                deviceId = jsnobject.getString("key");
            } catch (Exception e) {

            }
        }
        PreyAccountData newAccount = new PreyAccountData();
        newAccount.setApiKey(apiKey);
        newAccount.setDeviceId(deviceId);
        newAccount.setEmail(email);
        newAccount.setPassword(password);
        return newAccount;

    }

    public PreyAccountData registerNewDeviceWithApiKeyEmail(Context ctx, String apiKey, String email, String deviceType) throws PreyException {
        String deviceId = "";
        PreyHttpResponse responseDevice = registerNewDevice(ctx, apiKey, deviceType);
        String xmlDeviceId = responseDevice.getResponseAsString();
        //if json
        if (xmlDeviceId.contains("{\"key\"")) {
            try {
                JSONObject jsnobject = new JSONObject(xmlDeviceId);
                deviceId = jsnobject.getString("key");
            } catch (Exception e) {
            }
        }
        PreyLogger.i("deviceId:"+deviceId);
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
        if (response != null && response.getStatusCode() == 200) {
            PreyLogger.d("c2dm registry id set succesfully");
        }
        return response;
    }

    public boolean checkPassword(Context ctx, String apikey, String password) throws PreyException {
        PreyHttpResponse response= this.checkPassword(apikey, password, ctx);
        String xml = response.getResponseAsString();
        return xml.contains("<key");
    }

    private PreyHttpResponse checkPassword(String apikey, String password, Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        HashMap<String, String> parameters = new HashMap<String, String>();
        PreyHttpResponse response=null;
        try {
            String uri=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat("profile.xml");
            response = PreyRestHttpClient.getInstance(ctx).get(uri, parameters, apikey, password);
        } catch (Exception e) {
            throw new PreyException(ctx.getText(R.string.error_communication_exception).toString(), e);
        }
        if(response!=null&&response.getStatusCode()== HttpURLConnection.HTTP_UNAUTHORIZED){
            throw new PreyException(ctx.getText(R.string.password_wrong).toString());
        }
        try {
            PreyLogger.d("____[token]_________________apikey:"+apikey+" password:"+password);
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String uri2=PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("get_token.json");
            PreyHttpResponse response2 = PreyRestHttpClient.getInstance(ctx).get(uri2, parameters, apikey, password,"application/json");
            if(response2!=null) {
                JSONObject jsnobject = new JSONObject(response2.getResponseAsString());
                String tokenJwt = jsnobject.getString("token");
                PreyLogger.d("tokenJwt:" + tokenJwt);
                PreyConfig.getPreyConfig(ctx).setTokenJwt(tokenJwt);
            }else{
                PreyLogger.d("token: nulo");
            }

        } catch (Exception e) {

        }
        return response;
    }



    public String deleteDevice(Context ctx) throws PreyException {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        HashMap<String, String> parameters = new HashMap<String, String>();
        String xml;
        try {
            String url = this.getDeviceWebControlPanelUiUrl(ctx);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx)
                    .delete(url, parameters);
            PreyLogger.d(response.toString());
            xml = response.getResponseAsString();

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

    private String getVerifyUrl(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/verify.json");
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
        return getDeviceUrlApiv2(ctx).concat("/events.json");
    }

    private String getResponseUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/response.json");
    }

    public String getInfoUrlJson(Context ctx) throws PreyException {
        return getDeviceUrlApiv2(ctx).concat("/info.json");
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
                if (entityFiles.size() == 0){
                    preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters);
                }else {
                    preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, parameters, entityFiles);
                }
                PreyLogger.d("Data sent_: " + (preyHttpResponse==null?"":preyHttpResponse.getResponseAsString()));
            } catch (Exception e) {
                PreyLogger.e("Data wasn't send", e);
            }
        }
        return preyHttpResponse;
    }


    public boolean verify(Context ctx) throws Exception {
        boolean result = false;
        String url = getVerifyUrl(ctx);
        //PreyLogger.i("verify url:"+url);
        PreyHttpResponse preyHttpResponse = null;
        PreyConfig config = PreyConfig.getPreyConfig(ctx);
        preyHttpResponse = PreyRestHttpClient.getInstance(ctx).getAutentication(url,null);
        PreyLogger.d("status:"+preyHttpResponse.getStatusCode());
        result = (preyHttpResponse.getStatusCode() == 200);
        return result;
    }

    public void sendPreyHttpEvent(Context ctx, Event event, JSONObject jsonObject) {
        try {
            String url = getEventsUrlJson(ctx);
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("name", event.getName());
            parameters.put("info", event.getInfo());

            PreyLogger.d("sendPreyHttpEvent url:" + url);
            PreyLogger.d("name:" + event.getName() + " info:" + event.getInfo());
            PreyLogger.d("status:" + jsonObject.toString());
            String status = jsonObject.toString();
            PreyHttpResponse preyHttpResponse = PreyRestHttpClient.getInstance(ctx).postStatusAutentication(url, status, parameters);
            String jsonString = preyHttpResponse.getResponseAsString();
            if (jsonString != null && jsonString.length() > 0) {
                List<JSONObject> jsonObjectList = new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
                if (jsonObjectList != null && jsonObjectList.size() > 0) {
                    ActionsController.getInstance(ctx).runActionJson(ctx, jsonObjectList);
                }
            }
        } catch (Exception e) {
            PreyLogger.i("message:" + e.getMessage());
            PreyLogger.e("Event wasn't send", e);
        }
    }






    public String sendNotifyActionResultPreyHttp(Context ctx, Map<String, String> params) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String response = null;
        try {
            String url = getResponseUrlJson(ctx);
            PreyConfig.postUrl = null;
            PreyHttpResponse httpResponse = PreyRestHttpClient.getInstance(ctx).postAutentication(url, params);
            response = httpResponse.toString();
            PreyLogger.d("Notify Action Result sent: " + response);
        } catch (Exception e) {
            //PreyLogger.e("Notify Action Result wasn't send",e);
        }
        return response;
    }

    public String sendNotifyActionResultPreyHttp(Context ctx, String correlationId, Map<String, String> params) {
        return sendNotifyActionResultPreyHttp(ctx,null,correlationId,params);
    }
    public String sendNotifyActionResultPreyHttp(Context ctx, String status,String correlationId, Map<String, String> params) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        String response = null;
        try {
            String url = getResponseUrlJson(ctx);
            PreyConfig.postUrl = null;
            PreyHttpResponse httpResponse = PreyRestHttpClient.getInstance(ctx).postAutenticationCorrelationId(url, status,correlationId,params);
            response = httpResponse.toString();
            PreyLogger.d("Notify Action Result sent: " + response);
        } catch (Exception e) {
            //PreyLogger.e("Notify Action Result wasn't send",e);
        }
        return response;
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
            PreyLogger.i("Report sent: " + (preyHttpResponse==null?"":preyHttpResponse.getResponseAsString()));
        } catch (Exception e) {
            PreyLogger.e("Report wasn't send:" + e.getMessage(), e);
        }
        return preyHttpResponse;
    }

    public List<JSONObject> getActionsJsonToPerform(Context ctx) throws PreyException {
        String url = getDeviceUrlJson(ctx);
        //PreyLogger.i("url:"+url);
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
        String responseAsString = response.getResponseAsString();
        PreyLogger.d("responseAsString:" + responseAsString);
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
            sb=response.getResponseAsString();
            if (sb!=null)
                sb = sb.trim();
        }catch(Exception e){
            PreyLogger.e("Error, causa:" + e.getMessage(), e);
            return null;
        }
        PreyLogger.d("cmd:" + sb);
        return sb;
    }

    public void sendEvent(final Context ctx,final int id  ) {
        new Thread() {
            public void run() {


                PreyPhone phone=new PreyPhone(ctx);
                String serialNumber=phone.getHardware().getSerialNumber();

                String version=PreyConfig.getPreyConfig(ctx).getPreyVersion();
                String sid=PreyConfig.getPreyConfig(ctx).getSessionId();

                String time = "" + new Date().getTime();
                try {
                    String page = FileConfigReader.getInstance(ctx).getPreyEventsLogs();;
                    PreyLogger.d("URL:"+page);
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("enum", id);

                    JSONArray properties=new JSONArray();

                    JSONObject jsonSid = new JSONObject();
                    jsonSid.put("name", "sid");
                    jsonSid.put("value", sid);
                    properties.put(jsonSid);

                    JSONObject jsonSerial = new JSONObject();
                    jsonSerial.put("name", "sn");
                    jsonSerial.put("value", serialNumber);
                    properties.put(jsonSerial);


                    JSONObject jsonVersion = new JSONObject();
                    jsonVersion.put("name", "version");
                    jsonVersion.put("value", version);
                    properties.put(jsonVersion);

                    jsonParam.put("properties",properties);

                    PreyLogger.d("__________jsonParam:"+jsonParam.toString());

                    PreyRestHttpClient.getInstance(ctx).postJson(page, jsonParam);
                } catch (Exception e) {
                    PreyLogger.e("Error:" + e.getMessage(), e);
                }
            }
        }.start();
    }

    public void sendTree(final Context ctx,JSONObject json  ) throws PreyException{
        String uri = getDeviceUrlApiv2(ctx).concat("/data.json");
        PreyRestHttpClient.getInstance(ctx).postJsonAutentication(uri, json);
    }

    public int uploadFile(Context ctx, File file,String uploadID,long total)  throws PreyException{
        String uri = PreyConfig.getPreyConfig(ctx).getPreyUrl() + "upload/upload?uploadID=" + uploadID;
        return PreyRestHttpClient.getInstance(ctx).uploadFile(ctx,uri,file,total);
    }

    public FileretrievalDto uploadStatus(Context ctx,String uploadID)  throws Exception {
        FileretrievalDto dto=null;
        String uri = PreyConfig.getPreyConfig(ctx).getPreyUrl() + "upload/upload?uploadID=" + uploadID;
        PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).get(uri,null);
        String responseAsString = response.getResponseAsString();
        PreyLogger.d("uploadStatus resp:" + responseAsString);
        if (response.getStatusCode() == 200) {
            if (responseAsString != null ) {
                JSONObject jsnobject = new JSONObject(response.getResponseAsString());
                String id = jsnobject.getString("ID");
                String name = jsnobject.getString("Name");
                String size = jsnobject.getString("Size");
                String total = jsnobject.getString("Total");
                String status = jsnobject.getString("Status");
                String path = jsnobject.getString("Path");
                dto=new FileretrievalDto();
                dto.setFileId(id);
                dto.setName(name);
                dto.setSize(Long.parseLong(size));
                dto.setTotal(Long.parseLong(total));
                dto.setStatus(Integer.parseInt(status));
                dto.setPath(path);
            }
        }
        if (response.getStatusCode() == 404) {
            dto=new FileretrievalDto();
            dto.setStatus(response.getStatusCode());
        }
        return dto;
    }

    public String googlePlayVersion(Context ctx) {
        try {
            String uri = PreyConfig.getPreyConfig(ctx).getPreyGooglePlay();
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).get(uri, null);
            String responseAsString = response.getResponseAsString();
            int po = responseAsString.indexOf("softwareVersion\">");
            responseAsString = responseAsString.substring(po + 17);
            po = responseAsString.indexOf("</");
            responseAsString = responseAsString.substring(0, po);
            return responseAsString.trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String getUuidDevice(Context ctx){
        String uuid = null;
        try {
            String uri = getInfoUrlJson(ctx);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).getAutentication(uri, null);
            String out=response.getResponseAsString();
            JSONObject jsnobject = new JSONObject(out);
            uuid = jsnobject.getString("uuid");
        } catch (Exception e) {
        }
        return uuid;
    }

    public String getEmail(Context ctx) {
        String email = null;
        try {
            HashMap<String, String> parameters = new HashMap<String, String>();
            String apiv2 = FileConfigReader.getInstance(ctx).getApiV2();
            String url = PreyConfig.getPreyConfig(ctx).getPreyUrl().concat(apiv2).concat("profile.json");
            PreyLogger.d("url:" + url);
            PreyHttpResponse response = PreyRestHttpClient.getInstance(ctx).getAutentication(url, parameters);
            String out=response.getResponseAsString();
            JSONObject jsnobject = new JSONObject(out);
            email = jsnobject.getString("email");
            PreyLogger.d("email:"+email);
        } catch (Exception e) {
            PreyLogger.e("error get email", e);
        }
        return email;
    }

}