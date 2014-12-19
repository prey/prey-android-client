/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.prey.PreyStatus;
import com.prey.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
 

public class CongratulationsActivity extends PreyActivity {
	

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
		setContentView(R.layout.congratulations);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Bundle bundle = getIntent().getExtras();
		((TextView) findViewById(R.id.congrats_h2_text)).setText(bundle.getString("message"));

		getPreyConfig().registerC2dm();

		Button ok = (Button) findViewById(R.id.congrats_btn_ok);
		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(CongratulationsActivity.this, PreyConfigurationActivity.class);
				PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
				startActivity(intent);
				finish();
			}
		});
	}
}
