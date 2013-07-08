/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;


import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.PopUpAlertActivity;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class Alert extends JsonAction{


	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}
	
	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
			String title = "title";//parameters.getString("title");
			String description ="";
			try{
				description=parameters.getString("alert_message");
			}catch(JSONException je){
				description=parameters.getString("message");
			}

			Bundle bundle = new Bundle();
			bundle.putString("title_message", title);
			bundle.putString("description_message", description);

			Intent popup = new Intent(ctx, PopUpAlertActivity.class);
			popup.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			popup.putExtras(bundle);
			popup.putExtra("description_message", description);
			ctx.startActivity(popup);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","started"));
			try {
				int i = 0;
				while (!PreyStatus.getInstance().isPreyPopUpOnclick() && i < 10) {
					Thread.sleep(1000);
					i++;
				}
			} catch (InterruptedException e) {
			}
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","stopped"));
		} catch (JSONException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","failed",e.getMessage()));
		}
	}

}
