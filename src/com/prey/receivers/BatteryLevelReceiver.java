package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryLevelReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		/*
		int scale = -1;
		int level = -1;
		int voltage = -1;
		int temp = -1;

		level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		PreyLogger.d("BatteryLevelReceiver level is " + level + "/" + scale + ", temp is " + temp + ", voltage is " + voltage);

		
		HashMap<String, String> parameters=LocationUtil.dataLocation(ctx).getDataList();
		PreyLogger.d("lat:" + parameters.get(LocationUtil.LAT));
		PreyLogger.d("lng:" + parameters.get(LocationUtil.LNG));
		PreyLogger.d("acc:" + parameters.get(LocationUtil.ACC));
		*/
	}

}
