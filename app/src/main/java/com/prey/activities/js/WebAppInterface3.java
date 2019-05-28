/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.location.LocationUpdatesService;
import com.prey.actions.location.PreyLocation;

public class WebAppInterface3 extends WebAppInterface2{

    public WebAppInterface3(Context ctx){
        mContext=ctx;
    }

    @JavascriptInterface
    public String getData(){
        String ssid=PreyConfig.getPreyConfig(mContext).getSsid();
        String model = PreyConfig.getPreyConfig(mContext).getModel();
        String imei = PreyConfig.getPreyConfig(mContext).getImei();
        PreyLocation preyLocation=PreyConfig.getPreyConfig(mContext).getLocation();
        String lat=""+ LocationUpdatesService.round(preyLocation.getLat());
        String lng=""+ LocationUpdatesService.round(preyLocation.getLng());
        String public_ip= PreyConfig.getPreyConfig(mContext).getPublicIp().trim();
        String json="{\"lat\":\""+lat+"\",\"lng\":\""+lng+"\",\"ssid\":\""+ssid+"\",\"public_ip\":\""+public_ip+"\",\"imei\":\""+imei+"\",\"model\": \""+ model+"\"}";
        PreyLogger.i("getData:"+json);
        return json;
    }

}
