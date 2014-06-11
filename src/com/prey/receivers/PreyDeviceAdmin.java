/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.R;
public class PreyDeviceAdmin extends DeviceAdminReceiver {
	
    @Override
    public void onEnabled(Context context, Intent intent) {
        PreyLogger.d("Device Admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
    	/*PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
    	if(preyConfig.isRevokedPassword()){
    		String password=preyConfig.getRevokedPassword().trim();
    		 PreyLogger.d("Device Admin password:["+password+"]");
    		FroyoSupport.getInstance(context).changePasswordAndLock(password, true);
    	}else{*/
    		FroyoSupport.getInstance(context).lockNow();
    	/*}*/
        return context.getText(R.string.preferences_admin_enabled_dialog_message).toString();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    	PreyLogger.d("Device Admin disabled");
    }

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		PreyLogger.d("Password was changed successfully");
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
		if (preyConfig.isLockSet()){
			PreyLogger.d("Password was entered successfully");
	        preyConfig.setLock(false);
	        FroyoSupport.getInstance(context).changePasswordAndLock("", false);
	        final Context contexfinal=context;
	        new Thread(){
	            public void run() {
	            	PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(contexfinal, UtilJson.makeMapParam("stop","lock","stopped"));
	            }
	        }.start();
		}
	}

}
