/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager;

import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.events.Event;
import com.prey.events.factories.EventFactory;
import com.prey.events.retrieves.EventRetrieveDataBattery;
import com.prey.events.retrieves.EventRetrieveDataMobile;
import com.prey.events.retrieves.EventRetrieveDataNullMobile;
import com.prey.events.retrieves.EventRetrieveDataOnline;
import com.prey.events.retrieves.EventRetrieveDataUptime;
import com.prey.events.retrieves.EventRetrieveDataWifi;
import com.prey.managers.PreyConnectivityManager;

public class EventManager {

    private EventMap<String, JSONObject> mapData = null;
    private Context ctx = null;
    public Event event = null;
    public final static String ONLINE = "online";
    public final static String WIFI = "wifi";
    public final static String UPTIME = "uptime";
    public final static String PRIVATE_IP = "privateip";
    public final static String BATTERY = "battery";
    public final static String MOBILE = "mobile";

    public EventManager(Context ctx) {
        this.ctx = ctx;
    }

    public void execute(Event event) {
        boolean isDeviceRegistered = isThisDeviceAlreadyRegisteredWithPrey(ctx);
        String previousSsid = PreyConfig.getPreyConfig(ctx).getPreviousSsid();
        String ssid ="";
        boolean validation = true;
        String type_connect="";
        if (Event.WIFI_CHANGED.equals(event.getName())) {
            if(event.getInfo().indexOf("wifi")>0) {
                    for (int i = 0; i < 30; i++) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                        WifiManager wifiManager = (WifiManager) ctx.getSystemService(ctx.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        ssid = wifiInfo.getSSID();
                        PreyLogger.d("i[" + i + "]ssid:" + ssid);
                        if (!"<unknown ssid>".equals(ssid)) {
                            type_connect="wifi";
                            break;
                        }
                    }
            }
            if (ssid != null && !"".equals(ssid) && !ssid.equals(previousSsid) && !"<unknown ssid>".equals(ssid) && !"0x".equals(ssid)) {
                validation = true;
                new Thread() {
                    public void run() {
                        try {
                            EventFactory.sendLocationAware(ctx);
                        }catch (Exception e3){
                        }
                    }
                }.start();
            } else {
                validation = false;
            }
            if(event.getInfo().indexOf("mobile")>0){
                boolean isMobileConnected=false;
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    isMobileConnected=PreyConnectivityManager.getInstance(ctx).isMobileConnected();
                    PreyLogger.d("i[" + i + "]isMobileConnected:" + isMobileConnected);
                    if (isMobileConnected) {
                        String networkClass= PreyPhone.getNetworkClass(ctx);
                        PreyConfig.getPreyConfig(ctx).setPreviousSsid(networkClass);
                        event.setName(Event.MOBILE_CONNECTED);
                        event.setInfo(networkClass);
                        type_connect="mobile";
                        break;
                    }
                }
                validation = isMobileConnected;
            }
        }
        PreyLogger.d("EVENT name:" + event.getName() + " info:" + event.getInfo() + " ssid[" + ssid + "] previousSsid[" + previousSsid + "]");
        PreyLogger.d("validation:"+validation);
        if (validation) {
            PreyLogger.d("EVENT change PreviousSsid:" + ssid);
            PreyConfig.getPreyConfig(ctx).setPreviousSsid(ssid);
            // if This Device Already Registered With Prey
            if (isDeviceRegistered) {
                        PreyLogger.d("EVENT isDeviceRegistered");
                        this.mapData = new EventMap<String, JSONObject>();
                        this.event = event;
                        this.mapData.put(EventManager.ONLINE, null);
                        this.mapData.put(EventManager.UPTIME, null);
                        this.mapData.put(EventManager.WIFI, null);
                        this.mapData.put(EventManager.BATTERY, null);
                        this.mapData.put(EventManager.MOBILE, null);
                        new EventRetrieveDataOnline().execute(ctx, this);
                        new EventRetrieveDataUptime().execute(ctx, this);
                        new EventRetrieveDataWifi().execute(ctx, this);
                        new EventRetrieveDataBattery().execute(ctx, this);
                        if("mobile".equals(type_connect))
                            new EventRetrieveDataMobile().execute(ctx, this);
                        else
                            new EventRetrieveDataNullMobile().execute(ctx, this);
            }
        }
    }

    public void receivesData(String key, JSONObject data) {
        mapData.put(key, data);
        if (mapData.isCompleteData()) {
            sendEvents();
        }
    }

    private void sendEvents() {
        if (mapData != null) {
            JSONObject jsonObjectStatus = mapData.toJSONObject();
            PreyLogger.d("jsonObjectStatus: " + jsonObjectStatus.toString());
            if (event != null) {
                    String lastEvent = PreyConfig.getPreyConfig(ctx).getLastEvent();
                    PreyLogger.d("event name[" + this.event.getName() + "] lastEvent:"+lastEvent);
                    if (Event.BATTERY_LOW.equals(event.getName())) {
                        if(PreyConfig.getPreyConfig(ctx).isLocationLowBattery()) {
                            PreyLogger.d("LocationLowBatteryRunner.isValid(ctx):"+ LocationLowBatteryRunner.isValid(ctx));
                            if (LocationLowBatteryRunner.isValid(ctx)) {
                                new Thread(new LocationLowBatteryRunner(ctx)).start();
                                try{
                                    jsonObjectStatus.put("locationLowBattery",true);
                                }catch(Exception e){}
                            }
                        }
                    }
                    if (!Event.WIFI_CHANGED.equals(event.getName()) || !event.getName().equals(lastEvent)) {
                        PreyConfig.getPreyConfig(ctx).setLastEvent(event.getName());
                        PreyLogger.d("event name[" + this.event.getName() + "], info[" + this.event.getInfo() + "]");
                        new EventThread(ctx, event, jsonObjectStatus).start();
                    }
            }
        }
    }

    private boolean isThisDeviceAlreadyRegisteredWithPrey(Context ctx) {
        return PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey();
    }

}

