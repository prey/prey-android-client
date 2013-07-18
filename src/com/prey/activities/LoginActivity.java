/*******************************************************************************
m * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.prey.R;
import com.prey.activities.browser.manager.ManagerBrowser;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;

public class LoginActivity extends PasswordActivity {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Delete notifications (in case Activity was started by one of them)
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(R.string.preyForAndroid_name);
		startup();
		if (isThisDeviceAlreadyRegisteredWithPrey()) {
			 Event event=new Event(Event.APPLICATION_OPENED);
			 new Thread(new EventManagerRunner(getApplicationContext(),event));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		startup();
	}

	private void startup() {
		//TwilioPhoneManager.getInstance(getApplicationContext());
		ManagerBrowser managerBrowser=new ManagerBrowser();
		if (!isThisDeviceAlreadyRegisteredWithPrey()) {
			managerBrowser.preLogin(getApplicationContext());
		} else {
			if (isCamouflageSet()){
				showCamouflage();
			}else{
				managerBrowser.postLogin(getApplicationContext());
			}
		}
		finish();
	}
 

	private void showCamouflage() {
		Context ctx=getApplicationContext();
		Intent intent = null;
		intent = new Intent(ctx, CamouflageActivity.class);
		//intent = new Intent(ctx, GameSealsBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}

	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return getPreyConfig().isThisDeviceAlreadyRegisteredWithPrey(false);
	}

	private boolean isCamouflageSet() {
		return getPreyConfig().isCamouflageSet();
	}

}
