/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.events.manager.EventManager;
import com.prey.json.actions.PrivateIp;


public class EventRetrieveDataPrivateIp {

    public  void execute(Context context,EventManager manager){
        HttpDataService privateIpHttpDataService=  new PrivateIp().run(context, null, null);

        String privateIpData=privateIpHttpDataService.getSingleData();
        JSONObject privateIpJSon = new JSONObject();
        try {
            privateIpJSon.put("private_ip", privateIpData);
        } catch (JSONException e) {
        }
        PreyLogger.d("privateIp:"+privateIpData);
        manager.receivesData(EventManager.PRIVATE_IP, privateIpJSon);
    }
}