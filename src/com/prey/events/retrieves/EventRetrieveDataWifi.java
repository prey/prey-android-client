package com.prey.events.retrieves;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.events.manager.EventManager;
import com.prey.json.actions.Wifi;
 

import android.content.Context;

public class EventRetrieveDataWifi {

	public void execute(Context context,EventManager manager){
		HttpDataService wifiHttpDataService= new Wifi().run(context, null, null);		
		Map<String, String> wifiMapData=wifiHttpDataService.getDataList();
		JSONObject wifiJSon = new JSONObject();
		String ssid=wifiMapData.get(Wifi.SSID);
		try {
			wifiJSon.put("ssid_name",ssid );
		} catch (JSONException e) {
		}
		PreyLogger.d("wifi:"+ssid);
		manager.receivesData(EventManager.WIFI, wifiJSon);
	}
}
