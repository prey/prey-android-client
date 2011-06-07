package com.prey.receivers;

import java.util.ArrayList;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.LockAction;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.net.PreyWebServices;

public class PreyDeviceAdmin extends DeviceAdminReceiver {
	
    @Override
    public void onEnabled(Context context, Intent intent) {
        PreyLogger.d("Device Admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
    	FroyoSupport.getInstance(context).lockNow();
        return context.getText(R.string.preferences_admin_enabled_dialog_message).toString();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    	PreyLogger.d("Device Admin disabled");
    }

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		// TODO Auto-generated method stub
		PreyLogger.d("Password was changed successfully");
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
		if (preyConfig.isLockSet()){
			PreyLogger.d("Password was entered successfully");
			ArrayList<String> modulesList = new ArrayList<String>();
	        modulesList.add(LockAction.DATA_ID);
	        PreyWebServices.getInstance().deactivateModules(context,modulesList);
	        preyConfig.setLock(false);
	        FroyoSupport.getInstance(context).changePasswordAndLock("", false);
		}
	}

}
