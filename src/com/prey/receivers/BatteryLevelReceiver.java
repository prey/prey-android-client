package com.prey.receivers;

import java.util.HashMap;

import com.prey.PreyLogger;
import com.prey.actions.location.PreyLocation;
import com.prey.actions.location.PreyLocationManager;
import com.prey.services.LocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.BatteryManager;

public class BatteryLevelReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		int scale = -1;
		int level = -1;
		int voltage = -1;
		int temp = -1;

		level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		PreyLogger.d("BatteryLevelReceiver level is " + level + "/" + scale + ", temp is " + temp + ", voltage is " + voltage);

		ctx.startService(new Intent(ctx, LocationService.class));
		
		boolean validLocation = false;
		PreyLocation lastLocation=null;
		HashMap<String, String> parameters = new HashMap<String, String>(); 
		while (!validLocation) {
			lastLocation = PreyLocationManager.getInstance(ctx).getLastLocation();
			if (lastLocation.isValid()) {
				validLocation = true;
				parameters.put("lat", Double.toString(lastLocation.getLat()));
				parameters.put("lng", Double.toString(lastLocation.getLng()));
				parameters.put("acc", Float.toString(lastLocation.getAccuracy()));
				parameters.put("alt", Double.toString(lastLocation.getAltitude()));
			} else
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					PreyLogger.e("Thread was intrrupted. Finishing Location NotifierAction", e);
				}
		}
		PreyLogger.d("lat:" + Double.toString(lastLocation.getLat()));
		PreyLogger.d("lng:" + Double.toString(lastLocation.getLng()));
		PreyLogger.d("acc:" + Float.toString(lastLocation.getAccuracy()));
		PreyLogger.d("alt:" + Double.toString(lastLocation.getAltitude()));

	}

}
