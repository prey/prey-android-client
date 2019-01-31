/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.text.format.DateUtils;

import com.prey.PreyLogger;
import com.prey.actions.report.ReportService;

@TargetApi(21)
public class ReportJobService extends JobService {
    private static final int JOB_ID = 124;


    public static void schedule(Context ctx ) {
        JobScheduler jobScheduler = null;
        jobScheduler=(JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(ctx, ReportJobService.class.getName()));
        builder.setPeriodic(30 * DateUtils.MINUTE_IN_MILLIS );
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresDeviceIdle(false);
        int resulCode=jobScheduler.schedule(builder.build());
        if(resulCode==JobScheduler.RESULT_SUCCESS){
            PreyLogger.d("REPORT resulCode success");
        }else{
            PreyLogger.d("REPORT resulCode failed");
        }
    }

    public static void cancel(Context ctx) {
        JobScheduler jobScheduler =null;
        jobScheduler=(JobScheduler)ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        PreyLogger.d("REPORT onStartJob");
        new Thread(new Runnable() {
            public void run() {
                new ReportService().run(getApplicationContext());
                boolean needsReschedule = false;
                jobFinished(jobParameters, false);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        PreyLogger.d("REPORT onStopJob");
        jobFinished(jobParameters, false);
        return false;
    }
}