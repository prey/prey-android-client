package com.prey.managers;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PreyTelephonyManager {

 
	
	private TelephonyManager telephony=null;
	private static PreyTelephonyManager _instance = null;
	
	private PreyTelephonyManager(Context ctx) {
		telephony = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public static PreyTelephonyManager getInstance(Context ctx) {
		if (_instance == null)
			_instance = new PreyTelephonyManager(ctx);
		return _instance;
	}
	
	
	public boolean isDataConnectivityEnabled(){
		return telephony.getDataState()==TelephonyManager.DATA_CONNECTED;
	}
	
 

}
