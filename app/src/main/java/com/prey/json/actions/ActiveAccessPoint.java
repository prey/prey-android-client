/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class ActiveAccessPoint extends JsonAction {

    public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
        List<HttpDataService> listResult = super.report(ctx, list, parameters);
        return listResult;
    }

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyPhone phone = new PreyPhone(ctx);
        PreyPhone.Wifi wifiPhone = phone.getWifi();
        if (wifiPhone.isWifiEnabled()) {
            String ssid=wifiPhone.getSsid();
            HttpDataService data = null;
            if(!"".equals(ssid)&&!"<unknown ssid>".equals(ssid)) {
                data = new HttpDataService("active_access_point");
                data.setList(true);
                HashMap<String, String> parametersMap = new HashMap<String, String>();
                parametersMap.put("ssid", wifiPhone.getSsid());
                parametersMap.put("security", wifiPhone.getSecurity());
                parametersMap.put("mac_address", wifiPhone.getMacAddress());
                parametersMap.put("signal_strength", wifiPhone.getSignalStrength());
                parametersMap.put("channel", wifiPhone.getChannel());
                data.addDataListAll(parametersMap);
            }
            return data;
        } else {
            return null;
        }
    }
}
