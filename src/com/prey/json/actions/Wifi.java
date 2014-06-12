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

public class Wifi extends JsonAction {
	
	public static String SSID="ssid";

	@Override
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		HttpDataService data = new HttpDataService("wifi");
		try {

			data.setList(true);

			PreyPhone phone = new PreyPhone(ctx);
			com.prey.PreyPhone.Wifi wifiPhone = phone.getWifi();

			HashMap<String, String> parametersMap = new HashMap<String, String>();

		 

			parametersMap.put(SSID, wifiPhone.getSsid());
			parametersMap.put("mac_address", wifiPhone.getMacAddress());
			parametersMap.put("security", wifiPhone.getSecurity());
			parametersMap.put("signal_strength", wifiPhone.getSignalStrength());
			parametersMap.put("channel", wifiPhone.getChannel());

			parametersMap.put("interfaceType", wifiPhone.getInterfaceType());
			parametersMap.put("model", wifiPhone.getModel());
			parametersMap.put("vendor", wifiPhone.getVendor());
			parametersMap.put("ipAddress", wifiPhone.getIpAddress());
			parametersMap.put("gatewayIp", wifiPhone.getGatewayIp());
			parametersMap.put("netmask", wifiPhone.getNetmask());

			data.addDataListAll(parametersMap);

		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
		return data;
	}

}
