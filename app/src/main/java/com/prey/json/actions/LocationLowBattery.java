/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

import org.json.JSONObject;

import java.util.List;

public class LocationLowBattery extends JsonAction {

    public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters){
        return null;
    }

}