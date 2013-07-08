/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.backwardcompatibility;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.receivers.PreyDeviceAdmin;

@TargetApi(Build.VERSION_CODES.FROYO) 
public class FroyoSupport {

	private static FroyoSupport _instance;
	private static Context ctx;
	private DevicePolicyManager policyManager;
	private ComponentName deviceAdmin;
	
	public static FroyoSupport getInstance(Context context){
		if (_instance == null){
			_instance = new FroyoSupport();
			_instance.policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			try{
			_instance.deviceAdmin = new ComponentName(context, PreyDeviceAdmin.class);
			}catch(Exception e){
				PreyLogger.e("Error create PreyDeviceAdmin:"+e.getMessage(), e);
			}
		}
		return _instance;
	}

	
	public void changePasswordAndLock(String newPass, boolean lock){
	    try {
			if (isAdminActive()) {
				policyManager.resetPassword(newPass,DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
				if (lock)
					policyManager.lockNow();
			}
		} catch (Exception e) {
			PreyLogger.e("This device couldn't be locked. Honeycomb bug?", e);
		}
	}
	
	public void lockNow(){
	    if (isAdminActive())
	    	policyManager.lockNow();
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO) 
	public boolean isAdminActive(){
		return policyManager.isAdminActive(deviceAdmin);
	}
	
	public void removeAdminPrivileges(){
		policyManager.removeActiveAdmin(deviceAdmin);
	}
	
	
	
	public Intent getAskForAdminPrivilegesIntent(){
		PreyConfig.getPreyConfig(ctx).setSecurityPrivilegesAlreadyPrompted(true);
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,deviceAdmin);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		//intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getText(R.string.device_admin_prompt));
		return intent;
	}
	
	public void wipe() {
		if (isAdminActive())
			policyManager.wipeData(0);
		/*
			try {
				RecoverySystem.rebootWipeUserData(ctx);
			} catch (IOException e) {
				PreyLogger.e("Error wiping device", e);
			}
		*/
	}
	
}
