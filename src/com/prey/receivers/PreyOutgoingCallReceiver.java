package com.prey.receivers;

import com.prey.PreyLogger;
import com.prey.actions.camouflage.Camouflage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PreyOutgoingCallReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.NEW_OUTGOING_CALL";

	@Override
	public void onReceive(Context context, Intent intent) {
		PreyLogger.i("Out call Broadcast - Action received: "+intent.getAction());
		if (intent.getAction() != null && intent.getAction().equals(ACTION)) {
			
			String phoneNumber = getResultData();
		    if (phoneNumber != null) {
		      // No reformatted number, use the original
		      phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		      PreyLogger.i("phoneNumber:"+phoneNumber);
		      if("#12345*".equals(phoneNumber)){
		    	  Camouflage.hide(context, null, null);
		      }
		      if("#67890*".equals(phoneNumber)){
		    	  Camouflage.unhide(context, null, null);
		      }
		    }
		}
	}
}
