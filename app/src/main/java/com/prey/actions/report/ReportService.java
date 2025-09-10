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

import org.json.JSONArray;
import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyPhone;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;
import com.prey.receivers.AlarmReportReceiver;
import com.prey.util.ClassUtil;

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
				// Get the excluded reports from the prey config
				String exclude = PreyConfig.getPreyConfig(ctx).getExcludeReport();
				// Create a JSON array to store the reports to be sent
				JSONArray jsonArray = new JSONArray();
				// Log a debug message indicating the start of the report
				PreyLogger.d(String.format("REPORT start:%s", interval));
				// Add reports to the JSON array if they are not excluded
				if (!exclude.contains("picture"))
					jsonArray.put(new String("picture"));
				if (!exclude.contains("location"))
					jsonArray.put(new String("location"));
				if (!exclude.contains("access_points_list"))
					jsonArray.put(new String("access_points_list"));
				if (!exclude.contains("active_access_point"))
					jsonArray.put(new String("active_access_point"));
				// Try to execute the reports
				try {
					// Create a list to store the action results
					List<ActionResult> lista = new ArrayList<ActionResult>();
					// Iterate over the reports in the JSON array
					for (int i = 0; i < jsonArray.length(); i++) {
						// If the prey config is missing, execute the report
						if (PreyConfig.getPreyConfig(ctx).isMissing()) {
							String nameAction = jsonArray.getString(i);
							PreyLogger.d(String.format("nameAction:%s", nameAction));
							String methodAction = "report";
							JSONObject parametersAction = null;
							listData = ClassUtil.execute(ctx, lista, nameAction, methodAction, parametersAction, listData);
						}
					}
				} catch (Exception e) {
					PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
				}
				int parms = 0;
				for (int i = 0; listData != null && i < listData.size(); i++) {
					HttpDataService httpDataService = listData.get(i);
					parms = parms + httpDataService.getDataAsParameters().size();
					if (httpDataService.getEntityFiles() != null) {
						for (int j = 0; j < httpDataService.getEntityFiles().size(); j++) {
							EntityFile entity = httpDataService.getEntityFiles().get(j);
							if (entity != null && entity.getLength() > 0) {
								parms = parms + 1;
							}
						}
					}
				}
				// Check if the device is in a missing state
				if (PreyConfig.getPreyConfig(ctx).isMissing()) {
					// Check if there are any report parameters to send
					if (parms > 0) {
						// Send the report to the server using the PreyWebServices instance
						PreyHttpResponse response = PreyWebServices.getInstance().sendPreyHttpReport(ctx, listData);
						// Check if the response is not null
						if (response != null) {
							PreyConfig.getPreyConfig(ctx).setLastEvent("report_send");
							PreyLogger.d(String.format("REPORT response.getStatusCode():%s", response.getStatusCode()));
							if (200 == response.getStatusCode() || 201 == response.getStatusCode()) {
								PreyConfig.getPreyConfig(ctx).setTimeNextReport();
							}
							// Check if the response status code is 409 (Conflict)
							if (409 == response.getStatusCode()) {
								// Reset the report schedule
								ReportScheduled.getInstance(ctx).reset();
								PreyConfig.getPreyConfig(ctx).setMissing(false);
								PreyConfig.getPreyConfig(ctx).setIntervalReport("");
								PreyConfig.getPreyConfig(ctx).setExcludeReport("");
							}
						}
					}
				}
			} catch (Exception e) {
				PreyLogger.e(String.format("Error report:%s", e.getMessage()), e);
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "failed", null, UtilJson.makeMapParam("get", "report", "failed", e.getMessage()));
			}
		}
		return listData;
	}

}