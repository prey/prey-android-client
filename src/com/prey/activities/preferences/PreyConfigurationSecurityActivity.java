/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.preferences;

import android.app.AlertDialog;
 
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
 
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
 
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.preferences.DeviceAdminPreference;
 

public class PreyConfigurationSecurityActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreyLogger.i("onCreate");


		addPreferencesFromResource(R.xml.preferences_security);

		com.prey.preferences.DeviceAdminPreference deviceAdminPreference = (com.prey.preferences.DeviceAdminPreference)
				 getPreferenceManager().findPreference("PREFS_ADMIN_DEVICE");
		
		CheckBoxPreference checkboxPref = (CheckBoxPreference)
				 getPreferenceManager().findPreference("PREFS_ADMIN_DEVICE_REVOKED_CHECK");
		
		PreyConfig preyConfig = PreyConfig.getPreyConfig(this);
		if (preyConfig.isFroyoOrAbove()){
			if (FroyoSupport.getInstance(this).isAdminActive()){
				deviceAdminPreference.setTitle(R.string.preferences_admin_enabled_title);
				deviceAdminPreference.setSummary(R.string.preferences_admin_enabled_summary);
				 checkboxPref.setEnabled(true);
			} else {
				deviceAdminPreference.setTitle(R.string.preferences_admin_disabled_title);
				deviceAdminPreference.setSummary(R.string.preferences_admin_disabled_summary);
				 checkboxPref.setEnabled(false); 
			}
		} 
 
	    
	    
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		PreyLogger.i("onResume");
		

		 CheckBoxPreference checkboxPref = (CheckBoxPreference)
				 getPreferenceManager().findPreference("PREFS_ADMIN_DEVICE_REVOKED_CHECK");
		 
		PreyConfig preyConfig = PreyConfig.getPreyConfig(this);
		if (preyConfig.isFroyoOrAbove()){
			if (FroyoSupport.getInstance(this).isAdminActive()){
					 checkboxPref.setEnabled(true);
			}else{
				 checkboxPref.setEnabled(false); 
			}
		}
		 
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
		Intent intent = new Intent(PreyConfigurationSecurityActivity.this, PreyConfigurationActivity.class);
		PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
		startActivity(intent);
		finish();
	}
	
	private EditText userInput =null;

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
		PreyLogger.i("onPreferenceTreeClick");
		
 
		if (preference.getKey().equals("PREFS_ADMIN_DEVICE_REVOKED_CHECK")) {
			CheckBoxPreference check = (CheckBoxPreference) preference;
			if (check.isChecked()) {
			//	input = new EditText(this);
				//	input.setInputType(InputType.TYPE_CLASS_NUMBER);
				
				InputFilter[] filter = new InputFilter[1];
				filter[0] = new InputFilter.LengthFilter(4);
				//	input.setFilters(filter);
				PreyConfig preyConfig = PreyConfig.getPreyConfig(this);
				
				LayoutInflater li = LayoutInflater.from(this);
				View promptsView = li.inflate(R.layout.prompts_pin, null);
		 
				userInput = (EditText) promptsView
						.findViewById(R.id.editTextPin);
				userInput.setFilters(filter);
				userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
				userInput.setText(preyConfig.getDigitUninstallPin());
				
				//		input.setText(preyConfig.getDigitUninstallPin());
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.preferences_admin_device_revoked_password_dialog_title);
				builder.setView(promptsView);
				 
				builder.setPositiveButton(R.string.ok, new SecurityOkOnClickListener(this) );
				builder.setNegativeButton(R.string.cancel, new  SecurityCancelOnClickListener(this));

				builder.show();
			}else{
				PreyConfig preyConfig = PreyConfig.getPreyConfig(this);
				preyConfig.setUnInstallPin(false);
			}
		}
		return true;
	}
	
	public class SecurityOkOnClickListener implements OnClickListener{
		private Context ctx=null;
		public SecurityOkOnClickListener(Context ctx){
			super();
			this.ctx=ctx;
		}

		public void onClick(DialogInterface dialog, int which) {
			PreyLogger.i("text:"+userInput.getText());
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			preyConfig.setUnInstallPinDigit(true,userInput.getText().toString());
		}
		
	}
	
	public class SecurityCancelOnClickListener implements OnClickListener{
		private Context ctx=null;
		public SecurityCancelOnClickListener(Context ctx){
			super();
			this.ctx=ctx;
		}

		public void onClick(DialogInterface dialog, int which) {
			PreyLogger.i("text:"+userInput.getText());
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			preyConfig.setUnInstallPinDigit(false,userInput.getText().toString());
			
		}
		
	}

	/*
	 * public class CheckOnPreferenceChangeListener implements
	 * Preference.OnPreferenceChangeListener {
	 * 
	 * private Context ctx=null; public CheckOnPreferenceChangeListener(){
	 * super(); }
	 * 
	 * public CheckOnPreferenceChangeListener(Context ctx){ super();
	 * this.ctx=ctx; }
	 * 
	 * public boolean onPreferenceChange(Preference preference, Object newValue)
	 * { PreyLogger.i( "Pref " + preference.getKey() + " changed to " +
	 * newValue.toString()); Boolean resultado=(Boolean)newValue;
	 * RevokedPasswordPreferences editPref=
	 * (RevokedPasswordPreferences)getPreferenceManager
	 * ().findPreference("PREFS_ADMIN_DEVICE_REVOKED_PASSWORD");
	 * if(false&&resultado.booleanValue()){
	 * 
	 * editPref.setEnabled(true);
	 * 
	 * 
	 * AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	 * 
	 * builder.setMessage("Your Message");
	 * 
	 * builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	 * 
	 * public void onClick(DialogInterface dialog, int id) {
	 * 
	 * PreyLogger.i("onClick"); }
	 * 
	 * });
	 * 
	 * }else{ editPref.setEnabled(false);
	 * 
	 * } return true; }
	 * 
	 * }
	 */

}
