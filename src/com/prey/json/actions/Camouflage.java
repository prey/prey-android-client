package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class Camouflage extends JsonAction{
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}
	
	public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
		PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","camouflage","started"));
		PreyConfig.getPreyConfig(ctx).setCamouflageSet(true);
	}
	
	public void stop(Context ctx, List<ActionResult> lista, JSONObject options) {
		PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("stop","camouflage","stopped"));
		PreyConfig.getPreyConfig(ctx).setCamouflageSet(false);
	}
}
