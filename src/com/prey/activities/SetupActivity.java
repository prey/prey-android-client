/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyUtils;

public class SetupActivity extends PreyActivity {

	protected String getDeviceType(Context ctx) {
		return PreyUtils.getDeviceType(ctx);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(SetupActivity.this, WelcomeActivity.class);
		startActivity(intent);
		finish();
	}

}
