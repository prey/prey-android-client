/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.events.Event;
import com.prey.events.manager.EventManager;
import com.prey.json.actions.Wifi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class EventRetrieveDataMinWifi {

    public void execute(Context context,EventManager manager){
        HttpDataService wifiHttpDataService= new Wifi().run(context, null, null);
        Map<String, String> wifiMapData=wifiHttpDataService.getDataList();
        JSONObject wifiJSon = new JSONObject();
        String ssid=null;
        try {
            ssid=wifiMapData.get(Wifi.SSID);
            JSONObject accessElementJSon = new JSONObject();
            accessElementJSon.put("ssid", ssid);

            if (Event.WIFI_CHANGED.equals(manager.event.getName())){
                manager.event.setInfo(ssid);
            }
            wifiJSon.put("active_access_point", accessElementJSon);
            PreyConfig.getPreyConfig(context).setPreviousSsid(ssid);
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        PreyLogger.d("wifi:"+ssid);
        manager.receivesData(EventManager.WIFI, wifiJSon);
    }

}

