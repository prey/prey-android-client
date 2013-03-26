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

import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class FirstMacAddress  extends JsonAction{

	public ArrayList<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting FirstMacAddress Data.");
		ArrayList<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		PreyPhone phone=new PreyPhone(ctx);
		HttpDataService data = new HttpDataService("first_mac_address");
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put("first_mac_address",phone.getWifi().getMacAddress());
		data.getDataList().putAll(parametersMap);	
		return data;
	}
}
