package com.prey.managers;

import android.content.Context;
import android.net.ConnectivityManager;

public class PreyConnectivityManager {

 
	private ConnectivityManager connectivity=null;
	private static PreyConnectivityManager _instance = null;
	
	private PreyConnectivityManager(Context ctx) {
		connectivity = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public static PreyConnectivityManager getInstance(Context ctx) {
		if (_instance == null)
			_instance = new PreyConnectivityManager(ctx);
		return _instance;
	}
	
	
	public boolean isConnected(){
		return connectivity.getActiveNetworkInfo().isConnected();
	}

	public boolean isAvailable(){
		return connectivity.getActiveNetworkInfo().isAvailable();
	}
	
	public boolean isConnectedOrConnecting(){
		return connectivity.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	public boolean isFailover(){
		return connectivity.getActiveNetworkInfo().isFailover();
	}
	
	public boolean isRoaming(){
		return connectivity.getActiveNetworkInfo().isRoaming();
	}
}
 