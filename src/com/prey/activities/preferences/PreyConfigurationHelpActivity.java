/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.PreyConfigurationActivity;

public class PreyConfigurationHelpActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyLogger.i("onCreate");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setAccountVerified();
		addPreferencesFromResource(R.xml.preferences_help);

	}

	@Override
	protected void onResume() {
		super.onResume();

		PreyLogger.i("onResume");
		
		 
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		/*
		Preference p = findPreference("PREFS_ADMIN_DEVICE");
		if (preyConfig.isFroyoOrAbove()) {

			if (FroyoSupport.getInstance(getApplicationContext()).isAdminActive()) {
				p.setTitle(R.string.preferences_admin_enabled_title);
				p.setSummary(R.string.preferences_admin_enabled_summary);
			} else {
				p.setTitle(R.string.preferences_admin_disabled_title);
				p.setSummary(R.string.preferences_admin_disabled_summary);
			}
		} else
			p.setEnabled(false);
		*/
		Preference p = findPreference("PREFS_ABOUT");
		p.setSummary("Version " + preyConfig.getPreyVersion() + "-" + preyConfig.getPreyMinorVersion() + " © Fork Ltd.");
		 
		

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
		Intent intent = new Intent(PreyConfigurationHelpActivity.this, PreyConfigurationActivity.class);
		PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
		startActivity(intent);
		finish();
	}
}
