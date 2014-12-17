package com.prey.actions.report;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



import com.prey.PreyLogger;




import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

public class ReportScheduled {

	private static ReportScheduled instance = null;
	private ScheduledExecutorService scheduler = null;
	private Context context = null;

	private ReportScheduled(Context context) {
		this.context = context;

	}

	public static ReportScheduled getInstance(Context context) {
		if (instance == null) {
			instance = new ReportScheduled(context);
		}
		return instance;
	}
	

	@SuppressLint("NewApi")
	public void run(final int interval) {
		
		final Context ctx = context;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				PreyLogger.i("_____________start ReportScheduled");
				Intent intent = new Intent(ctx, ReportService.class);
				ctx.startService(intent);
			}
		}, 0, interval, TimeUnit.MINUTES);

	}

	public void reset() {
		if (scheduler != null){
			PreyLogger.i("_________________shutdown ReportScheduled");
			scheduler.shutdown();
		}
	}

}
