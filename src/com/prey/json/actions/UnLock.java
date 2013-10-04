/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class UnLock extends JsonAction{


	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}
	
 

	 
	
	public void sms(Context ctx, List<ActionResult> lista, JSONObject options) {
		 
		if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {
			PreyLogger.d("-- Unlock instruction received");
			FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop","lock","stopped"));
		}
	}

}
