package com.prey.services;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.activities.CreateAccountActivity;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class CreateAccountService extends IntentService {
	String error;

	public CreateAccountService() {
		super("CreateAccountService");
	}

	public void onHandleIntent(Intent intent) {
		String[] data = intent.getStringArrayExtra("params");
		try {
			PreyAccountData accountData = PreyWebServices.getInstance()
					.registerNewAccount(this, data[0], data[1], data[2], getDeviceType());
			PreyLogger.d("Response creating account: " + accountData.toString());
			getPreyConfig().saveAccount(accountData);
		} catch (PreyException e) {
			error = e.getMessage();
		}
		Intent resultIntent = new Intent(CreateAccountActivity.CREATEACCOUNT_FILTER);
		resultIntent.putExtra("error", error);
		sendBroadcast(resultIntent);
		return;
	}

	private String getDeviceType() {
		return PreyUtils.getDeviceType(this);
	}

	private PreyConfig getPreyConfig() {
		return PreyConfig.getPreyConfig(this);
	}
}
