/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import android.content.Context;
import com.prey.events.manager.EventManager;

import org.json.JSONException;
import org.json.JSONObject;

public class EventRetrieveDataNullMobile {

    public  void execute(Context context, EventManager manager){
        JSONObject mobileSon = new JSONObject();
        try {
            mobileSon.put("mobile_internet", "");
        } catch (Exception e) {
        }
        manager.receivesData(EventManager.MOBILE, mobileSon);
    }
}
