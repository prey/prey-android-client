package com.prey;

import android.app.Application;

/**
 * Created by oso on 25-08-15.
 */
public class PreyApp extends Application {

    public long mLastPause;

    @Override
    public void onCreate() {
        super.onCreate();
        try{
            mLastPause = 0;
            PreyLogger.d("__________________");
            PreyLogger.i("Application launched!");
            PreyLogger.d("__________________");
            String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();
            if (deviceKey != null && deviceKey != "")
                PreyConfig.getPreyConfig(this).registerC2dm();

        }catch(Exception e){}
    }
}
