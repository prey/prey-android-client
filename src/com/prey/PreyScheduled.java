package com.prey;

import android.annotation.SuppressLint;
import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.prey.beta.actions.PreyBetaController;

public class PreyScheduled {

	private static PreyScheduled instance = null;
	private ScheduledExecutorService scheduler = null;
	private Context context=null;

	private PreyScheduled(Context context) {
		this.context=context;
		run();
	}

	public static PreyScheduled getInstance(Context context) {
		if (instance == null) {
			instance = new PreyScheduled(context);
		}
		return instance;
	}

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.US);

	@SuppressLint("NewApi")
	private void run() {
		final Context ctx = context;
		if (PreyConfig.getPreyConfig(ctx).isScheduled()&&PreyConfig.getPreyConfig(ctx).getMinuteScheduled() > 0) {
			PreyLogger.i("_______"+PreyConfig.getPreyConfig(ctx).getMinuteScheduled());
			scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(new Runnable() {
				public void run() {
					if(PreyConfig.getPreyConfig(ctx).isScheduled()){
						PreyLogger.i("PreyScheduled:" + sdf.format(new Date()));
						PreyBetaController.startPrey(ctx,null);
					}else{
						PreyLogger.i("PreyScheduled reset");
						PreyScheduled.getInstance(ctx).reset();
					}
					
				}
			}, PreyConfig.getPreyConfig(ctx).getMinuteScheduled(), PreyConfig.getPreyConfig(ctx).getMinuteScheduled(), TimeUnit.MINUTES);
		}
	}

	public void reset() {
		if(scheduler!=null)
			scheduler.shutdown();
		run();
	}

}
