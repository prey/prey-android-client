/*******************************************************************************
 * Created by Carlos Yaconi
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
import com.prey.services.PreyKeepOnService;

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
		
		boolean keepOn = PreyConfig.getPreyConfig(getApplicationContext()).isKeepOn();
		if (keepOn) {
			startService(new Intent(getApplicationContext(), PreyKeepOnService.class));
		}else{
			stopService(new Intent(getApplicationContext(), PreyKeepOnService.class));
		}
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
		} else {
			if(getPreyConfig().showFeedback()){
				showFeedback(getApplicationContext());
			}else{
				if (!getPreyConfig().isCamouflageSet()){
					showLogin();
				}else{
					showCamouflage();
				}
			}
		}
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

	
	private void showFeedback(Context ctx){
		Intent popup = new Intent(ctx, FeedbackActivity.class);
		popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(popup);
	}
}
