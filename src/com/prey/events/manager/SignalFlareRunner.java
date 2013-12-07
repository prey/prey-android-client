package com.prey.events.manager;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.actions.observer.ActionsController;
import com.prey.events.Event;
import com.prey.json.parser.JSONParser;

public class SignalFlareRunner implements Runnable{

	private Context ctx=null;
	private Event event;
	
	public SignalFlareRunner(Context ctx,Event event){
		this.ctx=ctx;	
		this.event=event;
	}
	
	public void run() {
		if(event!=null){
			if ( Event.BATTERY_LOW.equals(event.getName())){
				
				String jsonString = "[ {\"command\": \"get\",\"target\": \"location\",\"options\": {}}]";
				List<JSONObject> jsonObjectList=new JSONParser().getJSONFromTxt(ctx, jsonString.toString());
				if (jsonObjectList!=null&&jsonObjectList.size()>0){
					ActionsController.getInstance(ctx).runActionJson(ctx,jsonObjectList);
				}
			}
		}
	}

}
