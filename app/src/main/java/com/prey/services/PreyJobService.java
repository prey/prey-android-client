/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2020 Prey Inc. All rights reserved.
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

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyScheduled;
import com.prey.PreyUtils;
import com.prey.beta.actions.PreyBetaActionsRunner;

@TargetApi(21)
public class PreyJobService extends JobService {
    private static final int JOB_ID = 321;

    public static int getMinuteScheduled(Context ctx){
        int minutes = 15;
        boolean isGooglePlayServicesAvailable = PreyUtils.isGooglePlayServicesAvailable(ctx);
        if (!isGooglePlayServicesAvailable) {
            minutes = PreyConfig.getPreyConfig(ctx).getMinutesToQueryServer();
        }
        return minutes;
    }
    public static void schedule(Context ctx ) {
        int minutes = getMinuteScheduled(ctx);
        PreyLogger.d("SCHEDULE minuteScheduled:"+minutes);
        if (minutes == 0) {
            PreyScheduled.getInstance(ctx).reset();
            cancel(ctx);
        } else {
            PreyScheduled.getInstance(ctx).run(minutes);
            JobScheduler jobScheduler = null;
            jobScheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(ctx, PreyJobService.class.getName()));
            builder.setPeriodic(minutes * DateUtils.MINUTE_IN_MILLIS);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setRequiresDeviceIdle(false);
            int resulCode = jobScheduler.schedule(builder.build());
            if (resulCode == JobScheduler.RESULT_SUCCESS) {
                PreyLogger.d("SCHEDULE resulCode success["+minutes+"]");
                try {
                    PreyBetaActionsRunner.getInstructionsNewThread(ctx, null,false);
                } catch (Exception e){
                }
            } else {
                PreyLogger.d("SCHEDULE resulCode failed");
            }
        }
    }

    public static void cancel(Context ctx) {
        PreyScheduled.getInstance(ctx).reset();
        JobScheduler jobScheduler =null;
        jobScheduler=(JobScheduler)ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
        PreyLogger.d("SCHEDULE cancel");
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        PreyLogger.d("SCHEDULE onStartJob");
        try {
            PreyBetaActionsRunner.getInstructionsNewThread(getApplicationContext(), null,false);
        } catch (Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        PreyLogger.d("SCHEDULE onStopJob");
        return true;
    }
}
