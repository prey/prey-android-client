/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.prey.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;

public class PermissionInformationBatchActivity extends PreyActivity {
	
	private static final int SECURITY_PRIVILEGES = 10;
	private String congratsMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Bundle bundle = getIntent().getExtras();
		congratsMessage = bundle.getString("message");
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (getPreyConfig().isFroyoOrAbove() && !FroyoSupport.getInstance(this).isAdminActive()){
			PreyLogger.i("Is froyo or above!!");
			Intent intent = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
			startActivityForResult(intent, SECURITY_PRIVILEGES);
		} else {
			showScreen();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SECURITY_PRIVILEGES)
			showScreen();
	}
	
	private void showScreen(){
		setContentView(R.layout.permission_information);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Button ok = (Button) findViewById(R.id.congrats_btn_ok);
		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(PermissionInformationBatchActivity.this, LoginActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("message", congratsMessage);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
	}
}