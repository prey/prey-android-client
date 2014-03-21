package com.prey.json.actions;

import java.util.List;

 
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;

import com.prey.actions.HttpDataService;
import com.prey.actions.location.LocationThread;
import com.prey.actions.location.LocationUtil;

import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
 

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
	
	public  List<HttpDataService> start(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting Location Data.");
		List<HttpDataService> listResult=super.get(ctx, list, parameters);
		return listResult;
	}
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		HttpDataService data = LocationUtil.dataLocation(ctx);
		return data;
	}
		
	public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting sms Location Data.");
		String phoneNumber = null;
		if (parameters != null) {
			try {
				phoneNumber = parameters.getString("parameter");
			} catch (Exception e) {
				try {
					phoneNumber = parameters.getString("phoneNumber");
				} catch (Exception e1) {
					PreyLogger.e("Error, causa:" + e1.getMessage(), e1);
				}
			}

			try {
				if (phoneNumber != null && !"".equals(phoneNumber)) {
					new LocationThread(ctx, phoneNumber).start();
				}
			} catch (Exception e) {
				PreyLogger.e("Error, causa:" + e.getMessage(), e);
			}
		}
	}

}
