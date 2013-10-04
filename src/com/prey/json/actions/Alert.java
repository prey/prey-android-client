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
 

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;

import com.prey.actions.alert.AlertThread;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class Alert extends JsonAction{

	
 

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}
	
	public void sms(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		String alert=null;
		try {
			alert = parameters.getString("parameter");
		} catch (Exception e) {
		}
		startAlert(ctx, alert);
	}
	
	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		String alert="";
		try{
			alert=parameters.getString("alert_message");
		}catch(Exception e){
			try{
				alert=parameters.getString("message");
			}catch(Exception e2){
			}
		}
		startAlert(ctx, alert);
	}
	
	public void startAlert(Context ctx, String alert) {
		try {
			if(alert!=null &&!"".equals(alert)){
				new AlertThread(ctx,alert).start();
			}
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","failed",e.getMessage()));
		}
	}
	
	 

}
