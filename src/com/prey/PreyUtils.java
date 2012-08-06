package com.prey;

import android.app.Activity;
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

}
