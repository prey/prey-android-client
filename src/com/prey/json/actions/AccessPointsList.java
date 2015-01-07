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
import com.prey.PreyPhone.Wifi;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.managers.PreyConnectivityManager;
import com.prey.net.PreyWebServices;

public class AccessPointsList extends JsonAction {

	public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		List<HttpDataService> listResult = super.report(ctx, list, parameters);
		return listResult;
	}

	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting AccessPointsList Data.");
		List<HttpDataService> listResult = super.get(ctx, list, parameters);
		return listResult;
	}

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		HttpDataService dataWifi = new HttpDataService("access_points_list");
		try {
			if (PreyConnectivityManager.getInstance(ctx).isWifiConnected()) {
				HashMap<String, String> parametersMapWifi = new HashMap<String, String>();
				PreyPhone preyPhone = new PreyPhone(ctx);
				List<Wifi> listWifi = preyPhone.getListWifi();
				for (int i = 0; listWifi != null && i < listWifi.size(); i++) {
					Wifi wifi = listWifi.get(i);
					parametersMapWifi.put(i + "][ssid", wifi.getSsid());
					parametersMapWifi.put(i + "][mac_address", wifi.getMacAddress());
					parametersMapWifi.put(i + "][security", wifi.getSecurity());
					parametersMapWifi.put(i + "][signal_strength", wifi.getSignalStrength());
					parametersMapWifi.put(i + "][channel", wifi.getChannel());
				}
				dataWifi.setList(true);
				dataWifi.getDataList().putAll(parametersMapWifi);
			}
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get", "access_points_list", "failed", e.getMessage()));
		}
		return dataWifi;
	}

}
