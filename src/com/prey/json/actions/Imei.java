/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class Imei extends JsonAction{

 
	public ArrayList<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting Imei Data.");
		ArrayList<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
 		HttpDataService data = new HttpDataService("imei");
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put("imei",imei);
		data.getDataList().putAll(parametersMap);	
		return data;
	}

}
