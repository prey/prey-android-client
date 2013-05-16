/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

 
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;

import com.prey.preferences.IconFullPreference;
import com.prey.preferences.IconPreference;
import com.prey.R;
public class PreyConfigurationActivity extends PreferenceActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyLogger.i("onCreate");
		PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
		preyConfig.setAccountVerified();
		addPreferencesFromResource(R.xml.preferences);
		 
		Resources res = getResources();
		

		
		
		IconPreference test0 = (IconPreference) findPreference("PREFS_STATUS");
		Drawable icon0 = res.getDrawable(R.drawable.ico_status);
		 test0.setIcon(icon0); 
		 
		IconPreference test4 = (IconPreference) findPreference("PREFS_HELP");
		Drawable icon4 = res.getDrawable(R.drawable.ico_help);
		 test4.setIcon(icon4);
		
		IconPreference test1 = (IconPreference) findPreference("PREFS_CONFIG");
		Drawable icon1 = res.getDrawable(R.drawable.ico_activation);
		 test1.setIcon(icon1);
	
		IconPreference test2 = (IconPreference) findPreference("PREFS_SECURITY");
		Drawable icon2 = res.getDrawable(R.drawable.ico_security);
		 test2.setIcon(icon2);
		
		IconPreference test3 = (IconPreference) findPreference("PREFS_ACCOUNT");
		Drawable icon3 = res.getDrawable(R.drawable.ico_account);
		 test3.setIcon(icon3);
		 	 
		 
		
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
		
		PreyLogger.i("onResume");
		/*
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
		p.setSummary("Version " + preyConfig.getPreyVersion() + "-" + preyConfig.getPreyMinorVersion() + " © Fork Ltd.");
		*/
	} 
	
 
	
	@Override
	protected void onPause() {
		
		super.onPause();
		PreyStatus.getInstance().setPreyConfigurationActivityResume(false);
		PreyLogger.i("onPause");
	}
	
	
	@Override
    protected void onStart(){
		super.onStart();
		PreyLogger.i("onStart");
	}
    
    protected void onRestart(){
    	super.onRestart();
		PreyLogger.i("onRestart");
	}

 
 
    protected void onStop(){
    	super.onStop();
		PreyLogger.i("onStop");
	}

    protected void onDestroy(){
    	super.onDestroy();
		PreyLogger.i("onDestroy");
	}
	
}
