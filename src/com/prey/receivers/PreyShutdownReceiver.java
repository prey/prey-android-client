package com.prey.receivers;

import com.prey.PreyConfig;
import com.prey.net.PreyWebServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PreyShutdownReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PreyConfig.getPreyConfig(context).unregisterC2dm(true);
		
	}

}
