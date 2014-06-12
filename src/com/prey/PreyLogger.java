/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.util.Log;

public class PreyLogger {

	public static void d(String message) {
		//if (PreyConfig.LOG_DEBUG_ENABLED)
			Log.i(PreyConfig.TAG,message);
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
