/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.PreyStatus;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.R;
public class PreyConfigurationActivity extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setAccountVerified();
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO Auto-generated method stub
		if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()){
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("EXIT", true);
			startActivity(intent);
			finish();
			
		}
		
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
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
		
		p = findPreference("PREFS_ABOUT");
		p.setSummary("Version " + preyConfig.getPreyVersion() + "-" + preyConfig.getPreyMinorVersion() + " � Fork Ltd.");
		
	} 
	
 
	
	@Override
	protected void onPause() {
		super.onPause();
		PreyStatus.getInstance().setPreyConfigurationActivityResume(false);
	}
	
	
 
	
}
