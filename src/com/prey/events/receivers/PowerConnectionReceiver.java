package com.prey.events.receivers;

import com.prey.events.Event;
import com.prey.events.factories.EventFactory;
import com.prey.events.manager.EventManagerRunner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) { 
    	/*
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
    
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        */
		Event event=EventFactory.getEvent(context, intent);
		new Thread(new EventManagerRunner(context,event));

    }
}

 
