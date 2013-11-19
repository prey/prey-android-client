package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.services.PreyKeepOnService;

public class Keepon {

	public void sms(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
            String parameter = parameters.getString("parameter");
            boolean keepOn= parameter!=null&&("on".equals(parameter)||"true".equals(parameter));
            PreyConfig.getPreyConfig(ctx).setKeepOn(keepOn);
            Intent intent = new Intent(ctx, PreyKeepOnService.class);
            if(keepOn){
            	 ctx.startService(intent);
            }else{
	             ctx.stopService(intent);
            }
		} catch (Exception e) {
            PreyLogger.i("Error causa:" + e.getMessage() );
		} 
	}
}
