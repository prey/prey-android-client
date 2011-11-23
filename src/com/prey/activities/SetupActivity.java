/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.util.DisplayMetrics;

public class SetupActivity extends Activity {
	
	protected String getDeviceType(){
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels; //320
		int height = dm.heightPixels; //480
		if (width >= 800 && height >= 480)
			return "Tablet";
		else
			return "Phone";
	}

}
