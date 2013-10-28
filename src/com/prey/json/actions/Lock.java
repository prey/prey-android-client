package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;

public class Lock {

	public void sms(Context ctx, List<ActionResult> lista, JSONObject parameters) {
        try {
                String unlock = parameters.getString("parameter");
                lock(ctx, unlock);
                
        } catch (Exception e) {
                PreyLogger.i("Error causa:" + e.getMessage() );
                //PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","lock","failed",e.getMessage()));
        }

	}
	
	public void lock(Context ctx, String unlock) {
        
        if (PreyConfig.getPreyConfig(ctx).isFroyoOrAbove()) {
                
                PreyConfig.getPreyConfig(ctx).setLock(true);
                PreyConfig.getPreyConfig(ctx).setUnlockPass(unlock);
                FroyoSupport.getInstance(ctx).changePasswordAndLock(unlock, true);
                //PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start","lock","started"));
        
        }


	}
	
}
