/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import com.testflightapp.lib.TestFlight;

import android.app.Application;

public class PreyApp extends Application {
	
	public long mLastPause;

    @Override
    public void onCreate() {
        super.onCreate();
        mLastPause = 0;
        PreyLogger.i("Application launched!");
		String deviceKey = PreyConfig.getPreyConfig(this).getDeviceID();
		if (deviceKey != null && deviceKey != "")
			PreyConfig.getPreyConfig(getApplicationContext()).registerC2dm();
		
		TestFlight.takeOff(this, "5f5efd93-49ac-4007-b3fd-1bb297032c9b");
		
    }
    
}
