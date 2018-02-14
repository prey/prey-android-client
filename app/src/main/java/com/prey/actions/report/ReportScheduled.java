/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.report;






import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.receivers.AlarmReportReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class ReportScheduled {

	private static ReportScheduled instance = null;
	private Context context = null;
	private AlarmManager alarmMgr = null;
	private PendingIntent pendingIntent = null;


	private ReportScheduled(Context context) {
		this.context = context;

	}

	public synchronized static ReportScheduled getInstance(Context context) {
		if (instance == null) {
			instance = new ReportScheduled(context);
		}
		return instance;
	}


	public void run() {
		  try {
			  int minute = Integer.parseInt(PreyConfig.getPreyConfig(context).getIntervalReport());

			  PreyLogger.d("----------ReportScheduled start minute:"+ minute);


			  Intent intent = new Intent(context, AlarmReportReceiver.class);

			  pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			  alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


			  if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
				  PreyLogger.d("----------setRepeating");
				  alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * minute, pendingIntent);
			  } else {
				  PreyLogger.d("----------setInexactRepeating");
				  alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() , 1000 * 60 * minute, pendingIntent);
			  }


			  PreyLogger.d("----------start report [" + minute + "] ReportScheduled");
		  }catch(Exception e){
			  PreyLogger.d("----------Error ReportScheduled :"+e.getMessage());
		  }

	}

	public void reset() {
		if (alarmMgr != null) {
			int minute =  Integer.parseInt(PreyConfig.getPreyConfig(context).getIntervalReport());

			PreyLogger.i("_________________shutdown report [" + minute + "] alarmIntent");
			alarmMgr.cancel(pendingIntent);
			minute = 0;
		}
	}

}
