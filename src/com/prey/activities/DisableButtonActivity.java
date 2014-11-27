/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.prey.PreyConfig;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.services.PreyDisablePowerOptionsService;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
 
 

public class DisableButtonActivity extends PreyActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

			showScreen();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			showScreen();
	}
	
	private void showScreen(){
		setContentView(R.layout.disable_power);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
		
		final CheckBox disable=(CheckBox)findViewById(R.id.disable_checkbox);
		disable.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
            	PreyConfig.getPreyConfig(getApplicationContext()).setDisablePowerOptions(disable.isChecked());
            	if(disable.isChecked()){
            		getApplicationContext().startService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
				}else{
					getApplicationContext().stopService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
				}
            	
            }
            
		});
		

		getPreyConfig().registerC2dm();

		Button ok = (Button) findViewById(R.id.congrats_btn_ok);
		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(DisableButtonActivity.this, LoginActivity.class);
				PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
				startActivity(intent);
				finish();
			}
		});
	}
}
