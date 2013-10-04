package com.prey.actions.location;

import java.util.Map;

import com.prey.actions.HttpDataService;

import android.content.Context;
import android.telephony.SmsManager;


public class LocationThread extends Thread {

	private Context ctx;
	private String phoneNumber;

	public LocationThread(Context ctx,String phoneNumber) {
		this.ctx = ctx;
		this.phoneNumber = phoneNumber;
	}
	
	public void run() {
		HttpDataService data=LocationUtil.dataLocation(ctx);
		if (phoneNumber!=null&&!"".equals(phoneNumber)){
			Map<String, String> parametersMap=data.getDataList();
		
			String lat=parametersMap.get("lat");
			String lng=parametersMap.get("lng");
			String message="location http://maps.google.com/?q="+lat+","+lng;
			sendSMS(phoneNumber,message);
		}

	}
	
	
	private void sendSMS(String phoneNumber, String message){
	       SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(phoneNumber, null, message, null, null);
	}
	

}
