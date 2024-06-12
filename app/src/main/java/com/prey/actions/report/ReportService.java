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

	public List<HttpDataService> run(Context ctx) {
		int interval=-1;
		List<HttpDataService> listData = new ArrayList<HttpDataService>();
		try{
			PreyLogger.d("REPORT _____________start ReportService");
			try {
				interval = Integer.parseInt(PreyConfig.getPreyConfig(ctx).getIntervalReport());
			}catch (Exception ee){
				interval=10;
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
			String exclude=PreyConfig.getPreyConfig(ctx).getExcludeReport();
			JSONArray jsonArray = new JSONArray();
			PreyLogger.d("REPORT start:"+interval);
			jsonArray = new JSONArray();
			if (!exclude.contains("picture"))
				jsonArray.put(new String("picture"));
			if (!exclude.contains("location"))
				jsonArray.put(new String("location"));
			if (!exclude.contains("access_points_list"))
				jsonArray.put(new String("access_points_list"));
			if (!exclude.contains("active_access_point"))
				jsonArray.put(new String("active_access_point"));
			try {
				List<ActionResult> lista = new ArrayList<ActionResult>();
				for (int i = 0; i < jsonArray.length(); i++) {
					if(PreyConfig.getPreyConfig(ctx).isMissing()) {
						String nameAction = jsonArray.getString(i);
						PreyLogger.d("nameAction:" + nameAction);
						String methodAction = "report";
						JSONObject parametersAction = null;
						listData = ClassUtil.execute(ctx, lista, nameAction, methodAction, parametersAction, listData);
					}
				}
			} catch (Exception e) {
				PreyLogger.e("Error:"+e.getMessage(),e);
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
			if(PreyConfig.getPreyConfig(ctx).isMissing()) {
				if (parms > 0) {
					PreyHttpResponse response = PreyWebServices.getInstance().sendPreyHttpReport(ctx, listData);
					if (response != null) {
						PreyConfig.getPreyConfig(ctx).setLastEvent("report_send");
						PreyLogger.d("REPORT response.getStatusCode():" + response.getStatusCode());
						if (409 == response.getStatusCode()) {
							ReportScheduled.getInstance(ctx).reset();
							PreyConfig.getPreyConfig(ctx).setMissing(false);
							PreyConfig.getPreyConfig(ctx).setIntervalReport("");
							PreyConfig.getPreyConfig(ctx).setExcludeReport("");
						}
					}
				}
			}
		} catch (Exception e) {
			PreyLogger.e("error report:"+e.getMessage(),e);
			PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"failed", null, UtilJson.makeMapParam("get", "report", "failed", e.getMessage()));
		}
		return listData;
	}

}