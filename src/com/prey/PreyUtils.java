/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class PreyUtils {
	
	public static String getDeviceType(Activity act){
		
		DisplayMetrics dm = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels; //320
		int height = dm.heightPixels; //480
		if (width >= 800 && height >= 480)
			return "Tablet";
		else
			return "Phone";
	}
	
	public static boolean supportSMS(Context ctx){
		//Froyo or above!!
		TelephonyManager telephonyManager1 = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        boolean isPhone = !(telephonyManager1.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE);
        boolean featureTelephony = ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
		return isPhone && featureTelephony;
	}

	public static String randomAlphaNumeric(int length){
		StringBuffer buffer = new StringBuffer();
		String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
		int charactersLength = characters.length();

		for (int i = 0; i < length; i++) {
			double index = Math.random() * charactersLength;
			buffer.append(characters.charAt((int) index));
		}
	    return buffer.toString();
	}
}
