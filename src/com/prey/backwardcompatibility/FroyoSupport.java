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
import android.telephony.TelephonyManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.receivers.PreyDeviceAdmin;

@TargetApi(Build.VERSION_CODES.FROYO)
public class FroyoSupport {

	private static FroyoSupport _instance;
	private Context ctx;
	private DevicePolicyManager policyManager;
	ComponentName deviceAdmin;
	
	public static FroyoSupport getInstance(Context context){
		if (_instance == null){
			_instance = new FroyoSupport(context);
		}
		return _instance;
	}

	private FroyoSupport(Context context){
		policyManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdmin = new ComponentName(context, PreyDeviceAdmin.class);
		ctx=context;
	}
	
	public void changePasswordAndLock(String newPass, boolean lock){
	    try {
			if (isAdminActive()) {
				boolean resultLocK=policyManager.resetPassword(newPass,DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
				PreyLogger.i("resultLocK:"+resultLocK);
				if (lock)
					lockNow();
			}
		} catch (Exception e) {
			PreyLogger.e("This device couldn't be locked. Honeycomb bug?", e);
		}
	}

	 
	public void lockNow(){
		if (isAdminActive())
			policyManager.lockNow();
	}
	
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
    }
    
    public static boolean supportSMS(Context ctx){
		TelephonyManager telephonyManager1 = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
    	boolean isPhone = !(telephonyManager1.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE);
		return isPhone ;
    }
	
}
