/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.json.UtilJson;
import com.prey.json.actions.Report;
import com.prey.net.PreyWebServices;
import com.prey.receivers.AlarmReportReceiver;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReportService extends IntentService {

	public ReportService() {
		super("reportService");
	}

	public ReportService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		run(this);
		stopSelf();
	}

	/**
	 * Runs the report service, which is responsible for sending reports to the server.
	 *
	 * @param ctx The application context.
	 * @return A list of HttpDataService objects containing the report data.
	 */
	public List<HttpDataService> run(Context ctx) {
		int interval = -1;
		List<HttpDataService> listData = new ArrayList<HttpDataService>();
		boolean isAirplaneModeOn = PreyPhone.isAirplaneModeOn(ctx);
		PreyLogger.d(String.format("AWARE AwareController init isAirplaneModeOn:%s", isAirplaneModeOn));
		boolean isTimeNextReport = PreyConfig.getPreyConfig(ctx).isTimeNextReport();
		PreyLogger.d(String.format("REPORT init isTimeNextReport:%s", isTimeNextReport));
		// Only proceed if location awareness is enabled and airplane mode is not on
		if (!isAirplaneModeOn && isTimeNextReport) {
			try {
				PreyLogger.d("REPORT _____________start ReportService");
				try {
					interval = Integer.parseInt(PreyConfig.getPreyConfig(ctx).getIntervalReport());
				} catch (Exception ee) {
					// If parsing fails, default to an interval of 10
					interval = 10;
				}
				//If it is Android 12 and you have alarm permission, run at the exact time
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					if (PreyPermission.canScheduleExactAlarms(ctx)) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(new Date().getTime());
						calendar.add(Calendar.MINUTE, interval);
						Intent intent = new Intent(ctx, AlarmReportReceiver.class);
						PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
						AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
						alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
					}
				}
				// Log a debug message indicating the start of the report
				PreyLogger.d(String.format("REPORT start:%s", interval));
				new Report().startReport(ctx,new JSONObject());
			} catch (Exception e) {
				PreyLogger.e(String.format("Error report:%s", e.getMessage()), e);
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "failed", null, UtilJson.makeMapParam("get", "report", "failed", e.getMessage()));
			}
		}
		return listData;
	}

}