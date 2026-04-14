/*******************************************************************************
 * Created by Prey
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class ServiceStartCompat {

    private ServiceStartCompat() {
    }

    public static void startServiceCompat(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
