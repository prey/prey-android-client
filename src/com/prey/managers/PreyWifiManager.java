package com.prey.managers;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class PreyWifiManager {

	private WifiManager wifiMgr=null;
	private static PreyWifiManager _instance = null;
	private Context ctx=null;
	
	private PreyWifiManager(Context ctx) {
		this.ctx=ctx;
		wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
	}
	
	public static PreyWifiManager getInstance(Context ctx) {
		if (_instance == null)
			_instance = new PreyWifiManager(ctx);
		return _instance;
	}

	public boolean isWifiEnabled(){
		return wifiMgr.isWifiEnabled();
	}
	
	public void setWifiEnabled(boolean enabled){
		wifiMgr.setWifiEnabled(enabled);
	}
	
	public List<ScanResult>  lista(){
		return  wifiMgr.getScanResults();
	}
	
	public  boolean isOnline() {
 
	    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public WifiInfo getConnectionInfo(){
		return wifiMgr.getConnectionInfo();
	}
	
	public String getSSID(){
		if (getConnectionInfo()!=null){
			String ssid= getConnectionInfo().getSSID();
			if(ssid!=null){
				ssid=ssid.replace("\"", "");
			}
			return ssid;
		}
		return null;
	}
 
	
}
