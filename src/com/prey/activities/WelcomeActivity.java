/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.prey.R;
public class WelcomeActivity extends PreyActivity {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		try{
			Button newUser = (Button) findViewById(R.id.btn_welcome_newuser);
		
		
			newUser.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, CreateAccountActivity.class);
				startActivity(intent);
				finish();
			}
		});
		}catch(Exception e){}
		
		try{
			Button oldUser = (Button) findViewById(R.id.btn_welcome_olduser);
			oldUser.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, AddDeviceToAccountActivity.class);
				startActivity(intent);
				finish();
			}
		});
		}catch(Exception e){}
	}

}
