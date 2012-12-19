package com.prey.json.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.location.PreyWifiManager;
import com.prey.actions.observer.ActionResult;

public class Access_points_list {

	public void get(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		PreyLogger.i(this.getClass().getName());
		try {

			HashMap<String, String> parametersMapWifi = new HashMap<String, String>();

			PreyWifiManager wifiManager = PreyWifiManager.getInstance(ctx);
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}
			List<ScanResult> listScan = wifiManager.lista();
			for (int i = 0; listScan != null && i < listScan.size(); i++) {
				ScanResult scan = listScan.get(i);
				parametersMapWifi.put("ssid", scan.SSID);
				parametersMapWifi.put("mac_address", scan.BSSID);
				parametersMapWifi.put("security", scan.capabilities);
				parametersMapWifi.put("signal_strength", String.valueOf(scan.level));
				parametersMapWifi.put("channel", String.valueOf(getChannelFromFrequency(scan.frequency)));
			}

			HttpDataService dataWifi = new HttpDataService("access_points_list");
			dataWifi.setList(true);
			dataWifi.getDataList().putAll(parametersMapWifi);

			ActionResult resultWifi = new ActionResult();
			resultWifi.setDataToSend(dataWifi);

			lista.add(resultWifi);
			PreyLogger.d("Ejecuting Access_points_list Action. DONE!");

		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}
	}

	
	private final static List<Integer> channelsFrequency = new ArrayList<Integer>(
	        Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447,
	                2452, 2457, 2462, 2467, 2472, 2484));

	public static Integer getFrequencyFromChannel(int channel) {
	    return channelsFrequency.get(channel);
	}

	public static int getChannelFromFrequency(int frequency) {
	    return channelsFrequency.indexOf(Integer.valueOf(frequency));
	}
	
}
