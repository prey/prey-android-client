package com.prey;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import com.prey.receivers.AlarmScheduledReceiver;

public class PreyScheduled {

	private static PreyScheduled instance = null;
	private Context context = null;
	private int minute = 0;

	private AlarmManager alarmMgr = null;
	private PendingIntent alarmIntent = null;

	private PreyScheduled(Context context) {
		this.context = context;
	}

	public synchronized static PreyScheduled getInstance(Context context) {
		if (instance == null) {
			instance = new PreyScheduled(context);
		}
		return instance;
	}

	public void run(int interval) {
		final Context ctx = context;
		if (minute != interval && PreyConfig.getPreyConfig(ctx).isScheduled() && interval > 0) {
			reset();
			minute = interval;
			Intent intent = new Intent(context, AlarmScheduledReceiver.class);
			alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * interval, alarmIntent);
			PreyLogger.i("_____________start scheduled [" + minute + "] alarmIntent");
		}
	}

	public void reset() {
		if (alarmMgr != null) {
			PreyLogger.i("_________________shutdown scheduled [" + minute + "]alarmIntent");
			alarmMgr.cancel(alarmIntent);
		}
	}

}
