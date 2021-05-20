/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.events.manager.EventManager;

import org.json.JSONObject;

public class EventRetrieveDataOnline {

    public  void execute(Context context,EventManager manager){
        JSONObject onlineJSon = new JSONObject();
        try {
            onlineJSon.put("online", true);
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        PreyLogger.d("online:true");
        manager.receivesData(EventManager.ONLINE, onlineJSon);
    }

}