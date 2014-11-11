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
import android.os.AsyncTask;
import android.os.Bundle;
 
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyScheduled;
import com.prey.R;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
public class WelcomeBatchActivity extends PreyActivity {

	private String error = null;
	
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

	
	private void installBatch() {
		new AddDeviceToApiKeyBatch().execute(getPreyConfig().getApiKeyBatch(),getPreyConfig().getEmailBatch(), getDeviceType());
	}

	private class AddDeviceToApiKeyBatch extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
			
		}
		
		@Override
		protected Void doInBackground(String... data) {
			try {
				error = null;
				PreyAccountData accountData =PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(WelcomeBatchActivity.this, data[0], data[1], data[2]);
				getPreyConfig().saveAccount(accountData);
			} catch (PreyException e) {
				error = e.getMessage();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void unused) {
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
				finish();
			}
		}
	}
}