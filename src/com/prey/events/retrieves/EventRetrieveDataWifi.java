package com.prey.events.retrieves;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.battery.Battery;
import com.prey.actions.battery.BatteryInformation;
import com.prey.events.manager.EventManager;
import com.prey.json.actions.Wifi;
 

import android.content.Context;

public class EventRetrieveDataWifi {

	public void execute(Context context,EventManager manager){
		HttpDataService wifiHttpDataService= new Wifi().run(context, null, null);		
		Map<String, String> wifiMapData=wifiHttpDataService.getDataList();
		JSONObject wifiJSon = new JSONObject();
		String ssid=null;
		try {
		 
			ssid=wifiMapData.get(Wifi.SSID);
			
			JSONObject accessElementJSon = new JSONObject();
			accessElementJSon.put("ssid", ssid);
			accessElementJSon.put("mac_address",wifiMapData.get("mac_address"));
			accessElementJSon.put("signal_strength",wifiMapData.get("signal_strength") );
			accessElementJSon.put("channel",wifiMapData.get("channel") );
			accessElementJSon.put("security", wifiMapData.get("security") );
	 
			
			

 

			
			wifiJSon.put("active_access_point", accessElementJSon);
			
			
			PreyConfig.getPreyConfig(context).setPreviousSsid(ssid);
		} catch (JSONException e) {
		}
		PreyLogger.d("wifi:"+ssid);
		manager.receivesData(EventManager.WIFI, wifiJSon);
		
		
		//active_access_point = { ssid: 'Starbucks',   mac_address: 'aa:11:22:af:00:21',  signal_strength: -50,   channel: 3, 	  security: 'WEP' 				}
		
		
		
		
		 
	}
}
