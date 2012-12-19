/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.preferences;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.actions.location.PreyLocationManager;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.services.PreyRunnerService;

public class PreyConfigurationStatusActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyLogger.i("onCreate");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setAccountVerified();
		addPreferencesFromResource(R.xml.preferences_status);

		
	 
		 


		
	}

	@Override
	protected void onResume() {
		super.onResume();
		

		PreyLogger.i("onResume");
		
		
		boolean isRunning = PreyRunnerService.running;
		String running = isRunning ? getString(R.string.running) : getString(R.string.stopped);
		
		
		Preference executionPrefs = (Preference) findPreference("PREFS_STATUS_EXECUTION");
		executionPrefs.setTitle(R.string.execution_status);
		executionPrefs.setSummary(running);
		
 
				
		boolean gps = PreyLocationManager.getInstance(getApplicationContext()).isGpsLocationServiceActive();
		boolean net = PreyLocationManager.getInstance(getApplicationContext()).isNetworkLocationServiceActive();
		
		String gpsActive = gps ? getString(R.string.enabled) : getString(R.string.disabled);
		String networkActive = net ? getString(R.string.enabled) : getString(R.string.disabled);
		
		Preference gpsPrefs = (Preference) findPreference("PREFS_STATUS_GPS");
		gpsPrefs.setTitle(R.string.gps_ls);
		gpsPrefs.setSummary(gpsActive);
				
		Preference networkPrefs = (Preference) findPreference("PREFS_STATUS_NETWORK");
		networkPrefs.setTitle(R.string.network_ls);
		networkPrefs.setSummary(networkActive);
		
		
		String smsActivation = PreyConfig.getPreyConfig(getApplicationContext()).getSmsToRun();
		String smsDeactivation = PreyConfig.getPreyConfig(getApplicationContext()).getSmsToStop();
		
		Preference smsActivationPrefs = (Preference) findPreference("PREFS_STATUS_SMS_ACTIVATION");
		smsActivationPrefs.setTitle(R.string.sms_activation);
		smsActivationPrefs.setSummary(smsActivation);
		
		Preference smsDeactivationPrefs = (Preference) findPreference("PREFS_STATUS_SMS_DEACTIVATION");
		smsDeactivationPrefs.setTitle(R.string.sms_deactivation);
		smsDeactivationPrefs.setSummary(smsDeactivation);
		
		 
         

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
		Intent intent = new Intent(PreyConfigurationStatusActivity.this, PreyConfigurationActivity.class);
		PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
		startActivity(intent);
		finish();
	}
}
