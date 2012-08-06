/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import com.prey.PreyUtils;

public class SetupActivity extends PreyActivity {
	
	protected String getDeviceType(){
		return PreyUtils.getDeviceType(this);
	}

}
