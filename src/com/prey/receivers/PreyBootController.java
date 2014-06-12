/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.json.actions.Report;
import com.prey.services.PreyDisablePowerOptionsService;

public class PreyBootController extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PreyLogger.d("Boot finished. Starting Prey Boot Service");
		// just make sure we are getting the right intent (better safe than
		// sorry)
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			String interval=PreyConfig.getPreyConfig(context).getIntervalReport();
			if (interval!=null&&!"".equals(interval)){
				Report.run(context,Integer.parseInt(interval));
			}
			boolean disablePowerOptions = PreyConfig.getPreyConfig(context).isDisablePowerOptions();
			if (disablePowerOptions) {
				context.startService(new Intent(context, PreyDisablePowerOptionsService.class));
			}else{
				context.stopService(new Intent(context, PreyDisablePowerOptionsService.class));
			}
		} else
			PreyLogger.e("Received unexpected intent " + intent.toString(),null);
	}
	
	
	
}
