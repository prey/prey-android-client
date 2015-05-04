package com.prey.services;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.activities.AddDeviceToAccountActivity;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class AddDeviceToAccountService extends IntentService {
	String error;
	boolean noMoreDeviceError;

	public AddDeviceToAccountService(String name) {
		super(name);
	}

	public void onHandleIntent(Intent intent) {
		String[] data = intent.getStringArrayExtra("params");
		try {
			noMoreDeviceError = false;
			error = null;
			PreyAccountData accountData = PreyWebServices.getInstance()
					.registerNewDeviceToAccount(this, data[0], data[1], data[2]);
			getPreyConfig().saveAccount(accountData);
		} catch (PreyException e) {
			error = e.getMessage();
			try {
				NoMoreDevicesAllowedException noMoreDevices = (NoMoreDevicesAllowedException) e;
				noMoreDeviceError = true;
			} catch (ClassCastException e1) {
				noMoreDeviceError = false;
			}
		}
		Intent resultIntent = new Intent(AddDeviceToAccountActivity.ADDDEVICE_FILTER);
		resultIntent.putExtra("error", error);
		resultIntent.putExtra("noMoreDeviceError", noMoreDeviceError);
		sendBroadcast(resultIntent);
		return;
	}

	private PreyConfig getPreyConfig() {
		return PreyConfig.getPreyConfig(this);
	}
}
