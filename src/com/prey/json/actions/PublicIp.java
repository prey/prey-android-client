/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class PublicIp extends JsonAction {

	public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		List<HttpDataService> listResult = super.report(ctx, list, parameters);
		return listResult;
	}

	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting PublicIp Data.");
		List<HttpDataService> listResult = super.get(ctx, list, parameters);
		return listResult;
	}

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		PreyPhone phone = new PreyPhone(ctx);
		HttpDataService data = new HttpDataService("public_ip");
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		String publicIp = phone.getIPAddress();
		parametersMap.put(publicIp, publicIp);
		data.setSingleData(publicIp);
		PreyLogger.d("public_ip:" + publicIp);
		;
		return data;
	}
}
