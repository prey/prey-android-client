package com.prey.receivers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.prey.actions.camouflage.Camouflage;
import com.prey.actions.camouflage.VolumeCounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
 

public class PreyOutgoingCallReceiver extends BroadcastReceiver {

 
	static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

	private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Override
	public void onReceive(Context context, Intent intent) {
		 
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
