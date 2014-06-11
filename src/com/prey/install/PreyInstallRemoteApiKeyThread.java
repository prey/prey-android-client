package com.prey.install;

import android.content.Context;

 
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyEmail;
import com.prey.PreyLogger; 
import com.prey.PreyUtils;
import com.prey.net.PreyWebServices;

public class PreyInstallRemoteApiKeyThread extends Thread {
	private Context ctx;
	private String pushedMessage;

	public PreyInstallRemoteApiKeyThread(Context ctx,String pushedMessage) {
		this.ctx = ctx;
		this.pushedMessage = pushedMessage;
	}

	public void run() {
		try {
			String email = PreyEmail.getEmail(ctx);
			PreyLogger.i("email:"+email +"pushedMessage:"+pushedMessage);
			String apiKey="";
			String deviceType = PreyUtils.getDeviceType(ctx);
			PreyAccountData accountData = null;
			accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, apiKey, email, deviceType);
			PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
		} catch (Exception e) {
			PreyLogger.i("Error: " + e.getMessage());

		}
	}

}
