/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

import org.json.JSONObject;

import java.util.List;

public class LocationLowBattery extends JsonAction {

    public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("Ejecuting LocationLowBattery Data.");
        List<HttpDataService> listResult=super.get(ctx, list, parameters);
        return listResult;
    }

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters){
        HttpDataService data = LocationUtil.dataLocation(ctx,null,false);
        data.setKey("location_low_battery");
        return data;
    }

}