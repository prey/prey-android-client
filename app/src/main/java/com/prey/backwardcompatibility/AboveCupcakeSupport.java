/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.backwardcompatibility;

import android.os.Build;

public class AboveCupcakeSupport {

    public static String getDeviceVendor() {

        return Build.MANUFACTURER;
    }

}
