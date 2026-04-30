/*******************************************************************************
 * Created by Patricio Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.retrieves;

import android.content.Context;

import com.prey.PreyLogger;

import org.json.JSONObject;

public class DeviceStatusCache {

    private static volatile JSONObject lastJson = null;
    private static volatile long lastUpdatedAt = 0L;

    private DeviceStatusCache() {}

    public static synchronized JSONObject get(Context ctx) {
        try {
            JSONObject fresh = DeviceStatusSnapshot.build(ctx);
            lastJson = fresh;
            lastUpdatedAt = System.currentTimeMillis();
            return fresh;
        } catch (Throwable t) {
            PreyLogger.e("build error, returning cache: " + t.getMessage(), t);
            return lastJson;
        }
    }

    public static long getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
