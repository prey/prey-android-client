package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;

public class Detach {

	
	public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
		FroyoSupport fSupport = FroyoSupport.getInstance(ctx);
		if (fSupport.isAdminActive()){
			fSupport.removeAdminPrivileges();
		}
		PreyConfig.getPreyConfig(ctx).unregisterC2dm(false);
		PreyConfig.getPreyConfig(ctx).setSecurityPrivilegesAlreadyPrompted(false);
		PreyConfig.getPreyConfig(ctx).wipeData();
	}
}
