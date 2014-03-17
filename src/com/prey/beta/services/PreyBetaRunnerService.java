/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.beta.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.prey.PreyConfig;
import com.prey.beta.actions.PreyBetaActionsRunnner;
import com.prey.services.LocationService;
import com.prey.actions.observer.ActionsController;


/**
 * This class wraps Prey execution as a services, allowing the OS to kill it and
 * starting it again in case of low resources. This way we ensure Prey will be
 * running until explicity stop it.
 * 
 * @author Carlos Yaconi H.
 * 
 */
public class PreyBetaRunnerService extends Service {

	private final IBinder mBinder = new LocalBinder();
	public static boolean running = false;
	public static long startedAt = 0;
	public static long interval = 0;
	public static long pausedAt = 0;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		PreyBetaRunnerService getService() {
			return PreyBetaRunnerService.this;
		}
	}

	@Override
	public void onCreate() {
		//PreyLogger.d("PreyRunnerService has been started...");
		PreyBetaActionsRunnner exec = new PreyBetaActionsRunnner();
		running = true;
		startedAt = System.currentTimeMillis();
		exec.run(PreyBetaRunnerService.this);
	}

	@Override
	public void onDestroy() {
		//PreyLogger.d("PreyRunnerService is going to be destroyed");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(PreyBetaRunnerService.this);
		preyConfig.setMissing(false);
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
		ActionsController.getInstance(PreyBetaRunnerService.this).finishRunningJosb();
		stopService(new Intent(PreyBetaRunnerService.this, LocationService.class));
		running = false;
		//PreyLogger.d("PreyRunnerService has been destroyed");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

}
