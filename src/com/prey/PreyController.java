/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;


import com.prey.services.PreyRunnerService;

public class PreyController {

	public static void startPrey(Context ctx) {
		PreyConfig config = PreyConfig.getPreyConfig(ctx);
		if (config.isThisDeviceAlreadyRegisteredWithPrey()){
			// Cancelling the notification of the SMS that started Prey
			NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancelAll();

			config.setRun(true);
			final Context context = ctx;
			new Thread(new Runnable() {
				
				public void run() {
					//First need to stop a previous running instance.
					context.stopService(new Intent(context, PreyRunnerService.class));
					context.startService(new Intent(context, PreyRunnerService.class));
				}
			}).start();
			
		}
	}

	public static void stopPrey(Context ctx) {
		ctx.stopService(new Intent(ctx, PreyRunnerService.class));
	}

}
