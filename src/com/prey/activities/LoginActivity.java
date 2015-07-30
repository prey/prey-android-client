/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.NotificationManager;

import com.prey.PreyStatus;
import com.prey.PreyVerify;
import com.prey.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.services.PreyDisablePowerOptionsService;

public class LoginActivity extends PasswordActivity {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Delete notifications (in case Activity was started by one of them)
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(R.string.preyForAndroid_name);
		startup();

		boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
		if (disablePowerOptions) {
			startService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
		} else {
			stopService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		startup();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startup();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		startup();
	}
	
	private void startup() {
		if (!isThisDeviceAlreadyRegisteredWithPrey()) {
			Intent intent = null;
			if (!isThereBatchInstallationKey()) {
				intent = new Intent(LoginActivity.this, WelcomeActivity.class);

			} else {
				intent = new Intent(LoginActivity.this, WelcomeBatchActivity.class);
			}
			startActivity(intent);
			finish();
		} else {
			PreyVerify.getInstance(this);
			if (getPreyConfig().showFeedback()) {
				showFeedback(getApplicationContext());
			} else {
				showLogin();
			}
		}
	}

	private void showLogin() {
		setContentView(R.layout.login);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		updateLoginScreen();

		Button gotoSettings = (Button) findViewById(R.id.login_btn_settings);

		if (!FroyoSupport.getInstance(this).isAdminActive()) {
			String h1 = getString(R.string.device_not_ready_h1);
			String h2 = getString(R.string.device_not_ready_h2);
			TextView textH1 = (TextView) findViewById(R.id.device_ready_h1_text);
			TextView textH2 = (TextView) findViewById(R.id.device_ready_h2_text);
			textH1.setText(h1);
			textH2.setText(h2);
		}

		try {
			Button gotoCP = (Button) findViewById(R.id.login_btn_cp);
			gotoCP.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					try {
						String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
						Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
						startActivity(browserIntent);
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}

		
		try {
			TextView uninstall = (TextView) findViewById(R.id.device_ready_uninstall);
			uninstall.setClickable(true);
			uninstall.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						
						String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyUninstallUrl();
						Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
						startActivity(browserIntent);
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}
		
		
		gotoSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
					Intent intent = new Intent(LoginActivity.this, CheckPasswordActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(LoginActivity.this, PreyConfigurationActivity.class);
					startActivity(intent);
				}
			}
		});
	}

	private boolean isThisDeviceAlreadyRegisteredWithPrey() {
		return getPreyConfig().isThisDeviceAlreadyRegisteredWithPrey(false);
	}

	private void showFeedback(Context ctx) {
		Intent popup = new Intent(ctx, FeedbackActivity.class);
		popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(popup);
	}

	private boolean isThereBatchInstallationKey() {
		String apiKeyBatch = getPreyConfig().getApiKeyBatch();
		return (apiKeyBatch != null && !"".equals(apiKeyBatch));
	}

}
