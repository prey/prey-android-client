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

public class RemainingStorage extends JsonAction{

 
	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting RemainingStorage Data.");
		List<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		HttpDataService data = new HttpDataService("remaining_storage");
 		PreyPhone phone=new PreyPhone(ctx);
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put("total",Long.toString(phone.getTotalMemory()));
		parametersMap.put("used",Long.toString(phone.getBusyMemory()));
		parametersMap.put("free",Long.toString(phone.getFreeMemory()));
		data.addDataListAll(parametersMap);	
		data.setList(true);
		return data;
	}
 

}
