/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.preferences.DelayedActivatedEditTextPreference;
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
		if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("EXIT", true);
			try {
				startActivity(intent);
			} catch (Exception e) { }
			
			finish();
		}

		final PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());

		Preference p = findPreference("PREFS_ADMIN_DEVICE");
		if (preyConfig.isFroyoOrAbove()) {

			if (FroyoSupport.getInstance(getApplicationContext()).isAdminActive()) {
				p.setTitle(R.string.preferences_admin_enabled_title);
				p.setSummary(R.string.preferences_admin_enabled_summary);
			} else {
				p.setTitle(R.string.preferences_admin_disabled_title);
				p.setSummary(R.string.preferences_admin_disabled_summary);
			}
		} else {
			p.setEnabled(false);
		}

		final Preference hiddenCP = findPreference("PREFS_ADVANCED_CATEGORY");
		final DelayedActivatedEditTextPreference pAbout = (DelayedActivatedEditTextPreference) findPreference("PREFS_ABOUT");
		pAbout.setSummary(getString(R.string.preferences_about_summary, preyConfig.getPreyVersion()));
		
		// hide advanced prefs when checkbox is unticked
		final CheckBoxPreference pCbAdvanced = (CheckBoxPreference) findPreference("PREFS_ENABLE_ADVANCED");
		pCbAdvanced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			// no need to listen to actual preference changes, handling click events is enough
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				getPreferenceScreen().removePreference(hiddenCP);
				pAbout.setCounter(0);
				return true;
			}
		});
		
		// show advanced settings on request
		pAbout.setOnPreferenceActivateListener(
				new DelayedActivatedEditTextPreference.OnPreferenceActivateListener() {
			@Override
			public boolean onPreferenceActivate(Preference preference) {
				preyConfig.setAdvancedPrefs(true);
				getPreferenceScreen().addPreference(hiddenCP);
				pCbAdvanced.setChecked(true);
				
				return false;
			}
		});
		if (!preyConfig.isAdvancedPrefsEnabled()) {
			getPreferenceScreen().removePreference(hiddenCP);
		} else {
			pAbout.setCounter(Integer.MAX_VALUE);
		}
		
		Preference pGo = findPreference("PREFS_GOTO_WEB_CONTROL_PANEL");
		pGo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
				PreyLogger.d("url control:" + url);
				Intent internetIntent = new Intent(Intent.ACTION_VIEW);
				internetIntent.setData(Uri.parse(url));
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);
				} catch (Exception e) { }
				
				return false;
			}
		});		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		PreyStatus.getInstance().setPreyConfigurationActivityResume(false);
	}
	
}
