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
import android.content.Intent;
 
 
import com.prey.PreyLogger;
 
import com.prey.actions.HttpDataService;
 
import com.prey.actions.location.PreyLocation;

import com.prey.actions.observer.ActionResult;
import com.prey.exceptions.PreyException;
import com.prey.json.JsonAction;
import com.prey.managers.PreyLocationManager;
import com.prey.services.LocationService;

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

	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		HttpDataService data = new HttpDataService("location");
		try {
			
			data.setList(true);
			
			
			ctx.startService(new Intent(ctx, LocationService.class));
			boolean validLocation = false;
			PreyLocation lastLocation;
			HashMap<String, String> parametersMap = new HashMap<String, String>();
			int i=0;
			while (!validLocation) {
				lastLocation = PreyLocationManager.getInstance(ctx).getLastLocation();
				if (lastLocation.isValid()) {
					validLocation = true;
					parametersMap.put("lat", Double.toString(lastLocation.getLat()));
					parametersMap.put("lng", Double.toString(lastLocation.getLng()));
					parametersMap.put("accuracy", Float.toString(lastLocation.getAccuracy()));
				//	parametersMap.put("alt", Double.toString(lastLocation.getAltitude()));
				} else{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						throw new PreyException("Thread was intrrupted. Finishing Location NotifierAction", e);
					}
					if (i>2){
						return null;
					}
					i++;
				}
			}

			data.addDataListAll(parametersMap);			
			
			

		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
		return data;
	}

}
