/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.PreyConfigurationActivity;

public class PreyConfigurationConfigActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyLogger.i("onCreate");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setAccountVerified();
		addPreferencesFromResource(R.xml.preferences_config);

	}

	@Override
	protected void onResume() {
		super.onResume();

		PreyLogger.i("onResume");

		

	}

	@Override
	protected void onPause() {

		super.onPause();
		PreyLogger.i("onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		PreyLogger.i("onStart");
	}

	protected void onRestart() {
		super.onRestart();
		PreyLogger.i("onRestart");
	}

	protected void onStop() {
		super.onStop();
		PreyLogger.i("onStop");
	}

	protected void onDestroy() {
		super.onDestroy();
		PreyLogger.i("onDestroy");
	}
	
	public void onBackPressed() {
		PreyLogger.i("onBackPressed");
		Intent intent = new Intent(PreyConfigurationConfigActivity.this, PreyConfigurationActivity.class);
		PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
		startActivity(intent);
		finish();
	}
}
