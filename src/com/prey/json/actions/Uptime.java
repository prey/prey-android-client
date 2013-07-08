/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.SystemClock;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class Uptime extends JsonAction{

 

	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting Uptime Data.");
		List<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		long uptime=SystemClock.uptimeMillis();
 		HttpDataService data = new HttpDataService("uptime");
		String uptimeData=Long.toString(uptime);
		data.setSingleData(uptimeData);
		return data;
	}

 

}
