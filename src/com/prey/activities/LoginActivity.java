/*******************************************************************************
m * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.NotificationManager;
import com.prey.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.prey.PreyConfig;
 

import com.prey.activities.browser.manager.ManagerBrowser;
import com.prey.events.Event;
import com.prey.events.manager.EventManager;

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
			new EventManager(getApplicationContext()).execute(new Event(Event.APPLICATION_OPENED));
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

	private void showLogin() {
		setContentView(R.layout.login);
		updateLoginScreen();
		Button gotoCP = (Button) findViewById(R.id.login_btn_cp);
		Button gotoSettings = (Button) findViewById(R.id.login_btn_settings);

		gotoCP.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(PreyConfig.CONTROL_PANEL_URL));
				startActivity(browserIntent);
			}
		});

		gotoSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, CheckPasswordActivity.class);
				startActivity(intent);
			}
		});
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
