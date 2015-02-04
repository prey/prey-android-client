/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import com.prey.actions.report.ReportScheduled;

import android.app.Application;

public class PreyApp extends Application {

	public long mLastPause;

	@Override
	public void onCreate() {
		super.onCreate();
		mLastPause = 0;
		PreyLogger.d("__________________");
		PreyLogger.i("Application launched!");
		PreyLogger.d("__________________");
		String deviceKey = PreyConfig.getPreyConfig(this).getDeviceID();
		if (deviceKey != null && deviceKey != "")
			PreyConfig.getPreyConfig(this).registerC2dm();
		if (PreyConfig.getPreyConfig(this).isScheduled()) {
			PreyScheduled.getInstance(this).run(PreyConfig.getPreyConfig(this).getMinuteScheduled());
		}
		if (PreyConfig.getPreyConfig(this).isMissing()) {
			if (PreyConfig.getPreyConfig(this).getIntervalReport() != null && !"".equals(PreyConfig.getPreyConfig(this).getIntervalReport())) {
				ReportScheduled.getInstance(this).run(Integer.parseInt(PreyConfig.getPreyConfig(this).getIntervalReport()));
			}
		}
	}
}
