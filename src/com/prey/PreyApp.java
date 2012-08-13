package com.prey;

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
    }
    
}
