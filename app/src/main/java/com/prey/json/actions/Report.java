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
            jobId = parameters.getString(PreyConfig.JOB_ID);
            PreyLogger.d("jobId:"+jobId);
        } catch (Exception e) {
        }
        String reason=null;
        if(jobId!=null&&!"".equals(jobId)){
            reason="{\"device_job_id\":\""+jobId+"\"}";
        }

        long lastReportStartDate = new Date().getTime();
        PreyLogger.d("____lastReportStartDate:" + lastReportStartDate);
        PreyConfig.getPreyConfig(ctx).setLastReportStartDate(lastReportStartDate);
        PreyConfig.getPreyConfig(ctx).setMissing(true);
        int interval = 0;
        try {
            PreyLogger.d("interval:" + parameters.getString("interval"));
            interval = parameters.getInt("interval");
        } catch (Exception e) {
            interval = 0;
        }
        String exclude = "";
        try {
            PreyLogger.d("exclude:" + parameters.getString("exclude"));
            exclude = parameters.getString("exclude");
        } catch (Exception e) {

        }
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        PreyConfig.getPreyConfig(ctx).setIntervalReport("" + interval);
        PreyConfig.getPreyConfig(ctx).setExcludeReport(exclude);

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
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        ReportScheduled.getInstance(ctx).reset();
        PreyConfig.getPreyConfig(ctx).setMissing(false);
        PreyConfig.getPreyConfig(ctx).setIntervalReport("");
        PreyConfig.getPreyConfig(ctx).setExcludeReport("");
        ReportJobService.cancel(ctx);

    }
    public boolean valida(Context ctx) {
        long lastReportStartDate = PreyConfig.getPreyConfig(ctx).getLastReportStartDate();
        PreyLogger.d("last:" + lastReportStartDate);
        if (lastReportStartDate != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lastReportStartDate);
            cal.add(Calendar.MINUTE, 1);
            long timeMore = cal.getTimeInMillis();
            PreyLogger.d("timM:" + timeMore);
            Date nowDate = new Date();
            long now = nowDate.getTime();
            PreyLogger.d("now_:" + now);
            PreyLogger.d("now>=timeMore:" + (now >= timeMore));
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

        }
    }

}

