package com.prey.services;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.preferences.DetachDevicePreferences;

public class DetachDeviceService extends IntentService {
	private String error;

	public DetachDeviceService() {
		super("DetachDeviceService");
	}

	public void onHandleIntent(Intent intent) {
		try {
			PreyConfig.getPreyConfig(this).unregisterC2dm(false);
			PreyConfig.getPreyConfig(this).setSecurityPrivilegesAlreadyPrompted(false);
			PreyWebServices.getInstance().deleteDevice(this);
			PreyConfig.getPreyConfig(this).wipeData();
		} catch (PreyException e) {
			e.printStackTrace();
			error = e.getMessage();
		}
		Intent resultIntent = new Intent(DetachDevicePreferences.DETACHDEVICE_FILTER);
		resultIntent.putExtra("error", error);
		sendBroadcast(resultIntent);
		return;
	}
}
