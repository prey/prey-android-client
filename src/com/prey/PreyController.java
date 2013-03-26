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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.prey.services.PreyRunnerService;

public class PreyController {

	public static void startPrey(Context ctx) {
		PreyConfig config = PreyConfig.getPreyConfig(ctx);
		if (config.isThisDeviceAlreadyRegisteredWithPrey(true)){
			// Cancelling the notification of the SMS that started Prey
			NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancelAll();
			// Since is missing, and we need to use http connections, we'll
			// wait phone to be ready to connect to control panel
			boolean isPhoneConnected = false;
			final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			final ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	
			try {
				isPhoneConnected = (tm.getDataState() == TelephonyManager.DATA_CONNECTED) || activeNetInfo.isConnected();
				while (!isPhoneConnected) {
					isPhoneConnected = (tm.getDataState() == TelephonyManager.DATA_CONNECTED) || activeNetInfo.isConnected();
					PreyLogger.d("Phone doesn't have internet connection now. Waiting 10 secs for it");
					Thread.sleep(10000);
				}
			} catch (InterruptedException e1) {
				PreyLogger.e("Can't wait for connection state. Execution will continue but we're not sure we could connect to internet", e1);
			} catch (NullPointerException npe) {
				PreyLogger.e("A manager couldn't be instanciated. Execution will continue but we're not sure we could connect to internet", npe);
			}
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
