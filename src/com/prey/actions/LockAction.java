/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionJob;
import com.prey.backwardcompatibility.FroyoSupport;

public class LockAction extends PreyAction {

	public static final String DATA_ID = "lock";
	public final String ID = "lock";
	
	public HttpDataService run(Context ctx) {
		return null;
	}

	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		return "";
	}

	@Override
	public void execute(ActionJob actionJob, Context ctx) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (preyConfig.isFroyoOrAbove()){
			preyConfig.setLock(true);
			FroyoSupport.getInstance(ctx).changePasswordAndLock(getConfig().get("unlock_pass"),true);
		}
	}

	@Override
	public boolean isSyncAction() {
		return false;
	}

	@Override
	public boolean shouldNotify() {
		return false;
	}
	
	@Override
	public void killAnyInstanceRunning(Context ctx) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (preyConfig.isFroyoOrAbove()) {
			PreyLogger.d("-- Unlock instruction received");
			FroyoSupport.getInstance(ctx).changePasswordAndLock("",true);
		}
//		if (PreyConfig.getPreyConfig(ctx).showLockScreen()){
//			PreyLogger.d("Instruction to kill Lock module received");
//			ctx.stopService(new Intent(ctx, LockMonitorService.class));
//			PreyConfig.getPreyConfig(ctx).setShowLockScreen(false);
//			Intent lockScreen = new Intent(ctx, LockActivity.class);
//			lockScreen.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			lockScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			ctx.startActivity(lockScreen);
//		}
		
		
	}
	
	public int getPriority(){
		return LOCK_PRIORITY;
	}

}
