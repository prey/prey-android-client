package com.prey.receivers;

import com.prey.PreyConfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppAddRemoveReceiver  extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		  if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			  PreyConfig.getPreyConfig(context).registerC2dm();
		  }
		
	}

	 

}
