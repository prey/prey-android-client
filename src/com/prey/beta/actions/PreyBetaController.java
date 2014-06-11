/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.actions;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;


import com.prey.PreyConfig;
import com.prey.beta.services.PreyBetaRunnerService;


public class PreyBetaController {

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
					context.stopService(new Intent(context, PreyBetaRunnerService.class));
					context.startService(new Intent(context, PreyBetaRunnerService.class));
				}
			}).start();
			
		}
	}

	public static void stopPrey(Context ctx) {
		ctx.stopService(new Intent(ctx, PreyBetaRunnerService.class));
	}

}
