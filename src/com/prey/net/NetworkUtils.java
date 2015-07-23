/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

 

public class NetworkUtils {

	private WifiManager wifiManager;
 
	private static NetworkUtils cachedInstance = null;
	private Context ctx;
	
	private NetworkUtils(Context ctx) {
		this.ctx=ctx;
		this.wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
 
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
	
	public boolean isMobileDataEnabled(){
	    
	    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

	    try {
	        Class<?> c = Class.forName(cm.getClass().getName());
	        Method m = c.getDeclaredMethod("getMobileDataEnabled");
	        m.setAccessible(true);
	        return (Boolean)m.invoke(cm);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

}
