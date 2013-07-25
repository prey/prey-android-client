package com.prey.json.actions;

 
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
 
import com.prey.PreyConfig;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
 
import com.prey.json.JsonAction;
 
import com.prey.services.RecorderService;

public class Video extends JsonAction{


 
 
    
 
 
	
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}
	 
	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		boolean videoStart=PreyConfig.getPreyConfig(ctx).getVideoStart();
		if (!videoStart){
			startRecording(ctx);
			videoStart=true;
		}else{
			stopRecording(ctx);
			videoStart=false;
		}
		PreyConfig.getPreyConfig(ctx).setVideoStart(videoStart);
	}
	
	
	public boolean startRecording(Context ctx){
		ctx.startService(new Intent(ctx, RecorderService.class));
		 
		 
		
		return true;
	}
	
	public boolean stopRecording(Context ctx){
		 ctx.stopService(new Intent(ctx, RecorderService.class));
		
		return true;
	}
	
	 
	

}
