/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import com.prey.PreyConfig;
import com.prey.PreyScheduled;
import com.prey.R;
import com.prey.services.AddDeviceToApiKeyBatch;
public class WelcomeBatchActivity extends PreyActivity {

	public static final String KEYBATCHRECEIVER_FILTER = "WelcomeBatchActivity_RECEIVER";

	private String error = null;
	private AddDeviceToApiKeyBatchReceiver receiver;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcomebatch);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		installBatch();
		
	 
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}
	
	private void installBatch() {
		receiver = new AddDeviceToApiKeyBatchReceiver();
		this.registerReceiver(receiver, new IntentFilter(KEYBATCHRECEIVER_FILTER));
		Intent addToKeyBatch = new Intent(this, AddDeviceToApiKeyBatch.class);
		String[] params = { getPreyConfig().getApiKeyBatch(),
				getPreyConfig().getEmailBatch(), getDeviceType() };
		addToKeyBatch.putExtra("params", params);
		this.startService(addToKeyBatch);
	}

	private class AddDeviceToApiKeyBatchReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context receiverContext, Intent receiverIntent) {
			error = receiverIntent.getStringExtra("error");
			if (error == null) {
				String message = getString(R.string.device_added_congratulations_text);
				Bundle bundle = new Bundle();
				bundle.putString("message", message);
				PreyConfig.getPreyConfig(WelcomeBatchActivity.this).setCamouflageSet(true);
				Intent intent = new Intent(WelcomeBatchActivity.this, PermissionInformationBatchActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				PreyConfig.getPreyConfig(getApplicationContext()).registerC2dm();
				if (PreyConfig.getPreyConfig(WelcomeBatchActivity.this).isScheduled()) {
					PreyScheduled.getInstance(WelcomeBatchActivity.this);
				}
				WelcomeBatchActivity.this.finish();
			}
		}
	}
}