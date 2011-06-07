package com.prey.backwardcompatibility;

import android.os.Build;

public class AboveCupcakeSupport {
	
	public static String getDeviceVendor() {
		
		return Build.MANUFACTURER;
	}

}
