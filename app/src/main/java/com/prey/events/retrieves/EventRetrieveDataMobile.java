/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.events.manager.EventManager;

import org.json.JSONException;
import org.json.JSONObject;

public class EventRetrieveDataMobile {

    public  void execute(Context context, EventManager manager){
        JSONObject mobileSon = new JSONObject();
        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            String mobile_internet=null;
            if(activeNetwork!=null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                try {
                    mobile_internet=PreyPhone.getNetworkClass(context);
                    PreyConfig.getPreyConfig(context).setPreviousSsid(mobile_internet);
                } catch (Exception e) {
                }
            }
            mobileSon.put("mobile_internet", mobile_internet);
            PreyLogger.d("mobile_internet:"+mobile_internet);
        } catch (Exception e) {
        }
        manager.receivesData(EventManager.MOBILE, mobileSon);
    }
}
