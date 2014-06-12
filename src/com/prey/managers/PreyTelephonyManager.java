package com.prey.managers;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PreyTelephonyManager {
 
	private String simSerialNumber=null;
	private TelephonyManager telephony=null;
	private static PreyTelephonyManager _instance = null;
	
	private PreyTelephonyManager(Context ctx) {
		telephony = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		simSerialNumber=telephony.getSimSerialNumber();
	}
	
	public static PreyTelephonyManager getInstance(Context ctx) {
		if (_instance == null)
			_instance = new PreyTelephonyManager(ctx);
		return _instance;
	}	
	
	public boolean isDataConnectivityEnabled(){
		return telephony.getDataState()==TelephonyManager.DATA_CONNECTED;
	}
	
	public int getSimState(){
		return telephony.getSimState();
	}
	
	public boolean isSimStateAbsent(){
		 return getSimState()==TelephonyManager.SIM_STATE_ABSENT;
	}
	
	public boolean isSimStateReady(){
		 return getSimState()==TelephonyManager.SIM_STATE_READY;
	}
	
	public String getSimSerialNumber(){
		return simSerialNumber;
	}
	public void setSimSerialNumber(String simSerialNumber){
		this.simSerialNumber=simSerialNumber;
	}	 
	 
	public String getLine1Number(){
		return telephony.getLine1Number();
		
	}
	
	
}
