package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class Lock extends JsonAction{


		public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
			return null;
		}
 

		public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
			try {
				String unlock = parameters.getString("unlock_pass");
				lock(ctx, unlock);
				 
			} catch (Exception e) {
				PreyLogger.e("Error causa:" + e.getMessage() + e.getMessage(), e);
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","lock","failed",e.getMessage()));
			}

		}

 
		
		public void stop(Context ctx, List<ActionResult> lista, JSONObject options) {
			 
			if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {
				PreyLogger.d("-- Unlock instruction received");
				FroyoSupport.getInstance(ctx).changePasswordAndLock("", true);
				WakeLock screenLock = ((PowerManager)ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(
					     PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
					screenLock.acquire();

					//later
					screenLock.release();
					
					
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("stop","lock","stopped"));
				PreyConfig.getPreyConfig(ctx).setLastEvent("lock_stopped");
			}
		}
	public void sms(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        try {
                String unlock = parameters.getString("parameter");
                lock(ctx, unlock);
                
        } catch (Exception e) {
                PreyLogger.i("Error causa:" + e.getMessage() );
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","lock","failed",e.getMessage()));
        }

	}
	
	public void lock(Context ctx, String unlock) {
        
        if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {
                
                PreyConfig.getPreyConfig(ctx).setLock(true);
                FroyoSupport.getInstance(ctx).changePasswordAndLock(unlock, true);
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","lock","started"));
                PreyConfig.getPreyConfig(ctx).setLastEvent("lock_started");
        }


	}
	
}
