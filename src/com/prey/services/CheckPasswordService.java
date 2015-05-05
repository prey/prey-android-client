package com.prey.services;

import android.app.IntentService;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.activities.PasswordActivity;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class CheckPasswordService extends IntentService {
	boolean isPasswordOk;
	String error;

	public CheckPasswordService() {
		super("CheckPasswordService");
	}

	public void onHandleIntent(Intent intent) {
		String[] password = intent.getStringArrayExtra("params");
		try {
			String email = getPreyConfig().getEmail();
			isPasswordOk = PreyWebServices.getInstance().checkPassword(this, email, password[0]);
		} catch (PreyException e) {
			error = e.getMessage();
		}
		Intent resultIntent = new Intent(PasswordActivity.CHECKPWD_FILTER);
		resultIntent.putExtra("error", error);
		resultIntent.putExtra("isPasswordOk", isPasswordOk);
		sendBroadcast(resultIntent);
		return;
	}

	private PreyConfig getPreyConfig() {
		return PreyConfig.getPreyConfig(this);
	}
}
