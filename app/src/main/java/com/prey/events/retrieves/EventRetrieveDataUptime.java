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
import com.prey.json.actions.Uptime;

public class EventRetrieveDataUptime {

    public  void execute(Context context,EventManager manager){
        HttpDataService uptimeHttpDataService=  new Uptime().run(context, null, null);
        String uptimeData=uptimeHttpDataService.getSingleData();
        JSONObject uptimeJSon = new JSONObject();
        try {
            uptimeJSon.put("uptime", uptimeData);
        } catch (JSONException e) {
        }
        PreyLogger.d("uptime:"+uptimeData);
        manager.receivesData(EventManager.UPTIME, uptimeJSon);
    }
}
