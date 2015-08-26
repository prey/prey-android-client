package com.prey.actions.report;

import java.util.Calendar;

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
	private PendingIntent alarmIntent = null;
	private int minute = 0;

	private ReportScheduled(Context context) {
		this.context = context;

	}

	public synchronized static ReportScheduled getInstance(Context context) {
		if (instance == null) {
			instance = new ReportScheduled(context);
		}
		return instance;
	}

	public void run(int interval) {
		if (minute != interval) {
			minute = interval;
			reset();
			Intent intent = new Intent(context, AlarmReportReceiver.class);
			alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * interval, alarmIntent);
			PreyLogger.i("_____________start report [" + minute + "] alarmIntent");
		}
	}

	public void reset() {
		if (alarmMgr != null) {
			PreyLogger.i("_________________shutdown report [" + minute + "] alarmIntent");
			alarmMgr.cancel(alarmIntent);
			minute = 0;
		}
	}

}
