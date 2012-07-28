package com.prey.net;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.prey.PreyLogger;

public class NetworkUtils {

	private WifiManager wifiManager;
	private ConnectivityManager mdataManager;
	private static NetworkUtils cachedInstance = null;
	
	private NetworkUtils(Context ctx) {
		this.wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		try {
			this.mdataManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			PreyLogger.e("Couldn't get an instance of ConnectivityManager", e);
		}
	}
	
	public static NetworkUtils getNetworkUtils(Context ctx) {
		if (cachedInstance==null){
			cachedInstance = new NetworkUtils(ctx);
		}
		return cachedInstance;
	}
	
	public boolean isWifiEnabled() {
		return wifiManager.isWifiEnabled();
	}
	
	public void turnOnWifi(boolean state) {
		wifiManager.setWifiEnabled(state);
	}
	
	public void enableMobileData(boolean state){
		try {
			Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
			dataMtd.setAccessible(true);
			dataMtd.invoke(this.mdataManager, state);
		} catch (Exception e) {
			PreyLogger.e("Couldn't enable mobile data", e);
		}
	}

}
