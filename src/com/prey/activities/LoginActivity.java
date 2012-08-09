/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.prey.PreyConfig;
import com.prey.R;

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
	}

	@Override
	protected void onStart() {
		super.onStart();
		startup();
	}

	private void startup() {
		if (!isThisDeviceAlreadyRegisteredWithPrey()) {
			Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
			startActivity(intent);
			finish();
		} else if (!getPreyConfig().isCamouflageSet())
			showLogin();
		else
			showCamouflage();
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

		setContentView(R.layout.camouflage);
		bindPasswordControls();
	}

	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return getPreyConfig().isThisDeviceAlreadyRegisteredWithPrey(false);
	}

	

}
