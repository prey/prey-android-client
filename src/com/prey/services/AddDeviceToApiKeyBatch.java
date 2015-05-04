package com.prey.services;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.activities.WelcomeBatchActivity;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class AddDeviceToApiKeyBatch extends IntentService {
	private String error;

	public AddDeviceToApiKeyBatch(String name) {
		super(name);
	}

	public void onHandleIntent(Intent intent) {
		String[] data = intent.getStringArrayExtra("params");
		try {
			error = null;
			PreyAccountData accountData = PreyWebServices.getInstance()
					.registerNewDeviceWithApiKeyEmail(this, data[0], data[1],
							data[2]);
			getPreyConfig().saveAccount(accountData);
		} catch (PreyException e) {
			error = e.getMessage();
		}
		Intent resultIntent = new Intent(WelcomeBatchActivity.KEYBATCHRECEIVER_FILTER);
		resultIntent.putExtra("error", error);
		sendBroadcast(resultIntent);
		return;
	}

	private PreyConfig getPreyConfig() {
		return PreyConfig.getPreyConfig(this);
	}
}
