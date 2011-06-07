package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.services.PreyBootService;

public class PreyBootController extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PreyLogger.d("Boot finished. Starting Prey Boot Service");
		// just make sure we are getting the right intent (better safe than
		// sorry)
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			context.startService(new Intent(context, PreyBootService.class));
		} else
			PreyLogger.e("Received unexpected intent " + intent.toString(),null);
	}
}