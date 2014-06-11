package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
 
import com.prey.PreyStatus;
import com.prey.actions.HttpDataService;
import com.prey.actions.alarm.AlarmThread;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
 

public class Alarm extends JsonAction{

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}

	public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
		String sound=null;
	    try {
	    	sound = parameters.getString("sound");
	    } catch (Exception e) {
	    }
		new AlarmThread(ctx,sound).start();		
	}

	public void stop(Context ctx, List<ActionResult> lista, JSONObject options) {
		PreyStatus.getInstance().setAlarmStop();
	}
	
	public void sms(Context ctx,List<ActionResult> lista,JSONObject parameters){
		this.start(ctx,lista,parameters);
	}
	
}
