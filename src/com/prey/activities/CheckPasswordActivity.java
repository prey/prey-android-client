/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

public class CheckPasswordActivity extends PasswordActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		updateLoginScreen();
		bindPasswordControls();
		if (!FroyoSupport.getInstance(this).isAdminActive()) {
			String h1 = getString(R.string.device_not_ready_h1);
			String h2 = getString(R.string.device_not_ready_h2);
			TextView textH1 = (TextView) findViewById(R.id.device_ready_h1_text);
			TextView textH2 = (TextView) findViewById(R.id.device_ready_h2_text);
			textH1.setText(h1);
			textH2.setText(h2);
		}
		KeyboardStatusDetector keyboard = new KeyboardStatusDetector();
		keyboard.registerActivity(this); // or register to an activity
		keyboard.setVisibilityListener(new KeyboardVisibilityListener() {
			@Override
			public void onVisibilityChanged(boolean keyboardVisible) {
				ImageView logoImgTextPrey = (ImageView) findViewById(R.id.logo_img_reversed);
				ImageView logoImgExtra = (ImageView) findViewById(R.id.logo_img_extra);
				TextView textReady = (TextView) findViewById(R.id.device_ready_h1_text);
				if (keyboardVisible) {
					PreyLogger.d("key on");
					if (logoImgTextPrey != null)
						logoImgTextPrey.setVisibility(View.GONE);
					if (logoImgExtra != null)
						logoImgExtra.setVisibility(View.GONE);
					if (textReady != null)
						textReady.setVisibility(View.GONE);
				} else {
					PreyLogger.d("key off");
					if (logoImgTextPrey != null)
						logoImgTextPrey.setVisibility(View.VISIBLE);
					if (logoImgExtra != null)
						logoImgExtra.setVisibility(View.VISIBLE);
					if (textReady != null)
						textReady.setVisibility(View.VISIBLE);
				}
			}
		});
	}

}
