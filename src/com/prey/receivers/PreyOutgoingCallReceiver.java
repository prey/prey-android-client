package com.prey.receivers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.prey.PreyLogger;
import com.prey.actions.camouflage.Camouflage;
import com.prey.actions.camouflage.VolumeCounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
 

public class PreyOutgoingCallReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.NEW_OUTGOING_CALL";
	static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//PreyLogger.d("Out call Broadcast - Action received: "+intent.getAction());
		if (intent.getAction() != null && intent.getAction().equals(ACTION)) {
			
			String phoneNumber = getResultData();
		    if (phoneNumber != null) {
		      // No reformatted number, use the original
		      phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		      PreyLogger.d("phoneNumber:"+phoneNumber);
		      if("#12345*".equals(phoneNumber)){
		    	  Camouflage.hide(context, null, null);
		      }
		      if("#67890*".equals(phoneNumber)){
		    	  Camouflage.unhide(context, null, null);
		      }
		    }
		}
		
		 
		if (intent.getAction() != null && intent.getAction().equals(VOLUME_CHANGED_ACTION)) {
			Bundle bu = intent.getExtras();
			if (bu!=null) {
				int volumen=bu.getInt("android.media.EXTRA_VOLUME_STREAM_VALUE");
				int prev=bu.getInt("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE");
				long timeUpdate=Long.parseLong(sdf.format(Calendar.getInstance().getTime()));
				if (VolumeCounter.getInstance().update(volumen, prev, timeUpdate)){
					if (volumen==0){
						Camouflage.hide(context, null, null);
					}else{
						Camouflage.unhide(context, null, null);
					}
					
				}
			}
		}
		
		
	}
	
	
}
