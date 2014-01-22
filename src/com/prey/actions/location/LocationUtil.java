package com.prey.actions.location;


import java.util.HashMap;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.exceptions.PreyException;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.services.LocationService;

public class LocationUtil {
	public static HttpDataService dataLocation(Context ctx) {

		HttpDataService data = new HttpDataService("location");
		try {

			data.setList(true);
			PreyConfig.getPreyConfig(ctx).setMissing(true);
			Intent intent = new Intent(ctx, LocationService.class);
			ctx.startService(intent);
			boolean validLocation = false;
			PreyLocation lastLocation;
			HashMap<String, String> parametersMap = new HashMap<String, String>();
			int i = 0;
			while (!validLocation) {
				lastLocation = PreyLocationManager.getInstance(ctx).getLastLocation();
				if (lastLocation.isValid()) {
					validLocation = true;
					parametersMap.put("lat", Double.toString(lastLocation.getLat()));
					parametersMap.put("lng", Double.toString(lastLocation.getLng()));
					parametersMap.put("accuracy", Float.toString(lastLocation.getAccuracy()));
				} else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						throw new PreyException("Thread was intrrupted. Finishing Location NotifierAction", e);
					}
					if (i > 2) {
						return null;
					}
					i++;
				}
			}

			data.addDataListAll(parametersMap);

			/*ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
			dataToBeSent.add(data);
			PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);*/

			ctx.stopService(intent);
			PreyConfig.getPreyConfig(ctx).setMissing(false);

		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() , e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,
			UtilJson.makeMapParam("get","location","failed",e.getMessage()));
		}
		return data;
	}
}
