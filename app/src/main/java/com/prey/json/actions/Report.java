/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Build;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.report.ReportScheduled;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.services.ReportJobService;

public class Report {

    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String jobId = null;
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID);
            PreyLogger.d(String.format("jobId:%s", jobId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        String reason = null;
        if (jobId != null && !"".equals(jobId)) {
            reason = "{\"device_job_id\":\"" + jobId + "\"}";
        }
        long lastReportStartDate = new Date().getTime();
        PreyLogger.d(String.format("____lastReportStartDate:%s", lastReportStartDate));
        PreyConfig.getPreyConfig(ctx).setLastReportStartDate(lastReportStartDate);
        PreyConfig.getPreyConfig(ctx).setMissing(true);
        int interval = 0;
        try {
            interval = UtilJson.getInt(parameters, "interval");
            PreyLogger.d(String.format("interval:%s", interval));
        } catch (Exception e) {
            interval = 0;
        }
        String exclude = "";
        try {
            exclude = UtilJson.getString(parameters, "exclude");
            PreyLogger.d(String.format("exclude:%s", exclude));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:s", e.getMessage()), e);
        }
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
            PreyLogger.d(String.format("messageId:%s", messageId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        PreyConfig.getPreyConfig(ctx).setIntervalReport("" + interval);
        PreyConfig.getPreyConfig(ctx).setExcludeReport(exclude);
        PreyConfig.getPreyConfig(ctx).removeTimeNextReport();
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("get", "report", "started",reason));
        PreyLogger.d("________start ReportScheduled");
        ReportScheduled.getInstance(ctx).run();
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ReportJobService.schedule(ctx);
        }
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("________stop Report");
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
            PreyLogger.d(String.format("messageId:%s", messageId));
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        ReportScheduled.getInstance(ctx).reset();
        PreyConfig.getPreyConfig(ctx).setMissing(false);
        PreyConfig.getPreyConfig(ctx).setIntervalReport("");
        PreyConfig.getPreyConfig(ctx).setExcludeReport("");
        ReportJobService.cancel(ctx);
    }

    public boolean valida(Context ctx) {
        long lastReportStartDate = PreyConfig.getPreyConfig(ctx).getLastReportStartDate();
        PreyLogger.d(String.format("last:%s", lastReportStartDate));
        if (lastReportStartDate != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lastReportStartDate);
            cal.add(Calendar.MINUTE, 1);
            long timeMore = cal.getTimeInMillis();
            PreyLogger.d(String.format("timM:%d", timeMore));
            Date nowDate = new Date();
            long now = nowDate.getTime();
            PreyLogger.d(String.format("now_:%d", now));
            PreyLogger.d(String.format("now>=timeMore:%d", (now >= timeMore)));
            return (now >= timeMore);
        }
        return true;
    }

    public static void run(Context ctx, int intervalReport) {
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("interval", intervalReport);
            new Report().get(ctx, null, parameters);
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
    }

}