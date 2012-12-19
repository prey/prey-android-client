package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.net.PreyWebServices;

public class Lock {

	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
			PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
			if (preyConfig.isFroyoOrAbove()) {

				String unlock = parameters.getString("password");

				preyConfig.setLock(true);
				preyConfig.setUnlockPass(unlock);
				FroyoSupport.getInstance(ctx).changePasswordAndLock(unlock, true);// getConfig().get("unlock_pass"),true);
				
				PreyWebServices.getInstance().sendEventsPreyHttpReport(ctx, "lock_started", "true");
			 
			}
		} catch (Exception e) {
			PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
		}

	}

	public void stop(Context ctx, List<ActionResult> lista, JSONObject options) {
		PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
		if (preyConfig.isFroyoOrAbove()) {
			PreyLogger.d("-- Unlock instruction received");
			FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
		}
	}

}
