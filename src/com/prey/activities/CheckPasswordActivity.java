/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.os.Bundle;
import com.prey.R;
import com.prey.analytics.PreyGoogleAnalytics;

public class CheckPasswordActivity extends PasswordActivity {

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
		updateLoginScreen();
		bindPasswordControls();
		
		PreyGoogleAnalytics.getInstance().trackAsynchronously(getApplicationContext(), "password");
		
	}

}
