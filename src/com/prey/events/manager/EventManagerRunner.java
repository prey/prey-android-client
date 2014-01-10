package com.prey.events.manager;



import com.prey.events.Event;

import android.content.Context;

public class EventManagerRunner implements Runnable{

	private Context ctx=null;
	private Event event;
	
	public EventManagerRunner(Context ctx,Event event){
		this.ctx=ctx;	
		this.event=event;
	}
	
	public void run() {
		if(event!=null){
			//PreyLogger.i("CheckInReceiver IN:" + event.getName());
			new EventManager(ctx).execute(event);
			//PreyLogger.i("CheckInReceiver OUT:" + event.getName());
		}
	}
}
