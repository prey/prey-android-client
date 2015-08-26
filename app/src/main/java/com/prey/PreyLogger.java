package com.prey;

import android.util.Log;

/**
 * Created by oso on 19-08-15.
 */
public class PreyLogger {


    public static void d(String message) {
        //if (PreyConfig.LOG_DEBUG_ENABLED)
            Log.i(PreyConfig.TAG, message);
    }

    public static void i(String message) {
        Log.i(PreyConfig.TAG,message);
    }

    public static void e(final String message, Throwable e) {
        if (e!=null)
            Log.e(PreyConfig.TAG, message, e);
        else
            Log.e(PreyConfig.TAG, message);
    }

}