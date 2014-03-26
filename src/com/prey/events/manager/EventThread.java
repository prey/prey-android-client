package com.prey.events.manager;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.events.Event;
import com.prey.net.PreyWebServices;

public class EventThread extends Thread  {

	private JSONObject jsonObjectStatus;
	
	private Event event;
	private Context ctx;
	
	public EventThread(Context ctx,Event event,JSONObject jsonObjectStatus){
		this.ctx=ctx;
		this.event=event;
		this.jsonObjectStatus=jsonObjectStatus;
	}
	 
	public void run() {
		 try {
			 boolean valida=EventControl.getInstance().valida(jsonObjectStatus);
			 PreyLogger.i("valida:"+valida+" eventName:"+event.getName());
			 if (valida){
				PreyWebServices.getInstance().sendPreyHttpEvent(ctx, event, jsonObjectStatus);
			 }
	     } catch (Exception e) {
	            e.printStackTrace();
	     }
	}

}
