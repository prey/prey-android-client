package com.prey.services;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.preferences.RevokedPasswordPreferences;

public class RevokedPasswordPhraseService extends IntentService {
	String error;

	public RevokedPasswordPhraseService() {
		super("RevokedPasswordPhraseService");
	}

	public void onHandleIntent(Intent intent) {
		String data = intent.getStringExtra("param");
		try {
			PreyConfig preyConfig = PreyConfig.getPreyConfig(this);
			PreyLogger.d("password [" + data + "]");
			preyConfig.setRevokedPassword(true, data);
		} catch (Exception e) {
			error = e.getMessage();
		}
		Intent resultIntent = new Intent(RevokedPasswordPreferences.REVOKEDPWD_FILTER);
		resultIntent.putExtra("error", error);
		sendBroadcast(resultIntent);
	}
}
