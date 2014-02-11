/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionsController;
import com.prey.services.PreyKeepOnService;

public class PreyBootController extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PreyLogger.d("Boot finished. Starting Prey Boot Service");
		// just make sure we are getting the right intent (better safe than
		// sorry)
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			//context.startService(new Intent(context, PreyBootService.class));
			reportBoot(context);
			boolean keepOn = PreyConfig.getPreyConfig(context).isKeepOn();
			if (keepOn) {
				context.startService(new Intent(context, PreyKeepOnService.class));
			}else{
				context.stopService(new Intent(context, PreyKeepOnService.class));
			}
		} else
			PreyLogger.e("Received unexpected intent " + intent.toString(),null);
	}
	
	
	public void reportBoot(Context ctx){
		final String interval=PreyConfig.getPreyConfig(ctx).getIntervalReport();
		if (interval!=null&&!"".equals(interval)){
			final Context myContext=ctx;
			new Thread(){
		            public void run() {
		            	try{
		            	Thread.sleep(90000);
						List<JSONObject> list=new ArrayList<JSONObject>();
						JSONObject jsonParams=new JSONObject();
						jsonParams.put("interval",interval );
						JSONObject json=new JSONObject();
						json.put("command", "get");
						json.put("target", "report");
						json.put("options",	jsonParams);
						list.add(json);
		            	ActionsController.getInstance(myContext).runActionJson(myContext,list);
		            	}catch(Exception e){
		    				
		    			}
		            }
		    }.start();
		}
	}
}
