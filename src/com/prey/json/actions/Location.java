/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

 

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

 
 
import com.prey.PreyLogger;
 
import com.prey.actions.HttpDataService;
 

import com.prey.actions.location.LocationThread;
import com.prey.actions.location.LocationUtil;


import com.prey.actions.observer.ActionResult;

import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;


public class Location extends JsonAction{

 
	public static final String DATA_ID = "geo";
	
	public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		List<HttpDataService> listResult=super.report(ctx, list, parameters);
		PreyLogger.d("Ejecuting Location reports. DONE!");
		return listResult;
	}
	
	public  List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting Location Data.");
		List<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	public  void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting sms Location Data.");
		try {
			String phoneNumber = parameters.getString("parameter");
			new LocationThread(ctx,phoneNumber).start();
		} catch (JSONException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("sms","location","failed",e.getMessage()));
		}
	}

	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		HttpDataService data = LocationUtil.dataLocation(ctx);
		return data;
	}

}
