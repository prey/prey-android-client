/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;

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
		// TODO Auto-generated method stub
		super.onResume();
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		Preference p = findPreference("PREFS_ADMIN_DEVICE");
		if (preyConfig.isFroyoOrAbove()){
			
			if (FroyoSupport.getInstance(getApplicationContext()).isAdminActive()){
				p.setTitle(R.string.preferences_admin_enabled_title);
				p.setSummary(R.string.preferences_admin_enabled_summary);
			} else {
				p.setTitle(R.string.preferences_admin_disabled_title);
				p.setSummary(R.string.preferences_admin_disabled_summary);
			}
		} else
			p.setEnabled(false);
	}
	

}
