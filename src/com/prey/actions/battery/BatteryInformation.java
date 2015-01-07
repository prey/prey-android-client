/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.battery;

 

import java.util.HashMap;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
 
 
 
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
 
 

public class BatteryInformation {

	public Battery battery=null;
	
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
	    @TargetApi(Build.VERSION_CODES.ECLAIR)
		@Override
	    public void onReceive(Context arg0, Intent intent) {
	    	int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
			int iconSmall = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			boolean present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
			int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
			int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			
			boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
			
			battery = new Battery();
			battery.setHealth(health);
			battery.setIconSmall(iconSmall);
			battery.setLevel(level);
			battery.setPlugged(plugged);
			battery.setPresent(present);
			battery.setScale(scale);
			battery.setStatus(status);
			battery.setTechnology(technology);
			battery.setTemperature(temperature);
			battery.setVoltage(voltage);
			battery.setCharging(charging);
			PreyLogger.d("voltage:"+voltage+" status:"+status+" technology:"+technology+" temperature:"+voltage);
			
	        arg0.unregisterReceiver(mBatInfoReceiver);
	    }

	};
	

	public HttpDataService getInformation (Context ctx) {
		
		battery=null;
	 
 
		//ctx.getApplicationContext().registerReceiver(batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		 
		ctx.getApplicationContext().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		try {
			int i = 0;
			while (battery == null && i <10) {
				Thread.sleep(1000);
				i++;
			}
		} catch (InterruptedException e) {
			PreyLogger.d("Error, causa:" + e.getMessage());
		} 
		 
		HttpDataService data =null;	
		 
			
		if (battery!=null){
			data = new HttpDataService("battery_status");
			HashMap<String, String> parametersMap = new HashMap<String, String>();
			parametersMap.put("state", battery.isCharging()?"charging":"discharging");
			parametersMap.put("remaining", Double.toString(battery.getLevel()));
			data.getDataList().putAll(parametersMap);	
			data.setList(true);
		} 
		
	//	ctx.getApplicationContext().unregisterReceiver(batteryInfoReceiver);
		 
		return data;
	}
 
	private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
		@TargetApi(Build.VERSION_CODES.ECLAIR)
		@Override
		public void onReceive(Context context, Intent intent) {
			int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
			int iconSmall = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			boolean present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
			int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
			int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			
			boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
			
			battery = new Battery();
			battery.setHealth(health);
			battery.setIconSmall(iconSmall);
			battery.setLevel(level);
			battery.setPlugged(plugged);
			battery.setPresent(present);
			battery.setScale(scale);
			battery.setStatus(status);
			battery.setTechnology(technology);
			battery.setTemperature(temperature);
			battery.setVoltage(voltage);
			battery.setCharging(charging);
		}
	};
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public Battery makeBattery(Intent intent){
		int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
		int iconSmall = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
		boolean present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
		String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
		int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
		int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
		
		boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
		
		Battery battery = new Battery();
		battery.setHealth(health);
		battery.setIconSmall(iconSmall);
		battery.setLevel(level);
		battery.setPlugged(plugged);
		battery.setPresent(present);
		battery.setScale(scale);
		battery.setStatus(status);
		battery.setTechnology(technology);
		battery.setTemperature(temperature);
		battery.setVoltage(voltage);
		battery.setCharging(charging);
		return battery;
	}
 
}
