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
import com.prey.actions.autoconnect.AutoConnectService;
import com.prey.actions.aware.AwareController;

@TargetApi(21)
public class AutoconnectJobService extends JobService {
    private static final int JOB_ID = 126;


    public static void schedule(Context ctx ) {
        JobScheduler jobScheduler = null;
        jobScheduler=(JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(ctx, AutoconnectJobService.class.getName()));
        builder.setPeriodic(20 * DateUtils.MINUTE_IN_MILLIS );
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresDeviceIdle(false);
        int resulCode=jobScheduler.schedule(builder.build());
        if(resulCode==JobScheduler.RESULT_SUCCESS){
            PreyLogger.d("AUTO resulCode success");
        }else{
            PreyLogger.d("AUTO resulCode failed");
        }
    }


    public static void cancel(Context ctx) {
        JobScheduler jobScheduler =null;
        jobScheduler=(JobScheduler)ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        PreyLogger.d("AUTO onStartJob");
        new Thread(new Runnable() {
            public void run() {
                new AutoConnectService().run(getApplicationContext());
                boolean needsReschedule = false;
                jobFinished(jobParameters, false);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        PreyLogger.d("AUTO onStopJob");
        jobFinished(jobParameters, false);
        return false;
    }
}
