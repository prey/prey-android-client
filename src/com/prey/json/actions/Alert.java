package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import com.prey.PreyLogger;
import com.prey.actions.alert.AlertThread;
import com.prey.actions.observer.ActionResult;

import android.content.Context;

public class Alert {

	public void sms(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        String alert=null;
        try {
                alert = parameters.getString("parameter");
        } catch (Exception e) {
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
                 //PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","alert","failed",e.getMessage()));
         }
	 }
}
