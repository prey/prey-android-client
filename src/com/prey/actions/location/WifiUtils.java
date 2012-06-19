package com.prey.actions.location;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiUtils {

	private WifiManager wifiManager;
	private static WifiUtils cachedInstance = null;
	
	private WifiUtils(Context ctx) {
		this.wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
	}
	
	public static WifiUtils getWifiUtils(Context ctx) {
		if (cachedInstance==null){
			cachedInstance = new WifiUtils(ctx);
		}
		return cachedInstance;
	}
	
	public boolean isWifiEnabled() {
		return wifiManager.isWifiEnabled();
	}
	
	public void turnOnWifi(boolean state) {
		wifiManager.setWifiEnabled(state);
	}

}
