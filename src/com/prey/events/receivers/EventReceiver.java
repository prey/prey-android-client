package com.prey.events.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
 
import com.prey.events.manager.EventManager;
 

public class EventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		new EventManager(context).run(intent);
		
		
	}
	
	
}
