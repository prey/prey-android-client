/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.accessibility.AccessibilityManager;

/**
 * Registers system listeners that fire on permission state changes and triggers
 * PermissionsReporter.sendIfChanged() in response. Lives for the lifetime of
 * the app process (started from PreyApp.onCreate).
 *
 * Coverage:
 *   - AppOps (location/camera/storage/etc): AppOpsManager.OnOpChangedListener
 *   - exact alarms: ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED (API 31+)
 *   - accessibility services: AccessibilityServicesStateChangeListener (API 33+)
 *
 * Permissions without a system listener (drawOverlays, MANAGE_EXTERNAL_STORAGE
 * on older APIs) are caught by Activity.onResume() and the periodic check at
 * process start.
 */
public class PermissionsWatcher {

    private static volatile boolean started = false;

    private static final String[] WATCHED_OPS_BASE = {
            AppOpsManager.OPSTR_FINE_LOCATION,
            AppOpsManager.OPSTR_COARSE_LOCATION,
            AppOpsManager.OPSTR_CAMERA,
            AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE,
            AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE,
    };

    private PermissionsWatcher() {}

    public static synchronized void start(Context ctx) {
        if (started || ctx == null) return;
        Context appCtx = ctx.getApplicationContext();
        registerAppOps(appCtx);
        registerExactAlarmReceiver(appCtx);
        registerAccessibilityListener(appCtx);
        started = true;
    }

    private static void registerAppOps(final Context ctx) {
        try {
            AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps == null) return;
            AppOpsManager.OnOpChangedListener listener = new AppOpsManager.OnOpChangedListener() {
                @Override
                public void onOpChanged(String op, String packageName) {
                    if (packageName != null && packageName.equals(ctx.getPackageName())) {
                        PreyLogger.d("AppOp changed:" + op);
                        PermissionsReporter.sendIfChanged(ctx);
                    }
                }
            };
            for (String op : WATCHED_OPS_BASE) {
                appOps.startWatchingMode(op, ctx.getPackageName(), listener);
            }
        } catch (Exception e) {
            PreyLogger.e("Error registering AppOps listener:" + e.getMessage(), e);
        }
    }

    private static void registerExactAlarmReceiver(final Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        try {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    PreyLogger.d("exact alarm permission changed");
                    PermissionsReporter.sendIfChanged(ctx);
                }
            };
            IntentFilter filter = new IntentFilter(
                    AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ctx.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ctx.registerReceiver(receiver, filter);
            }
        } catch (Exception e) {
            PreyLogger.e("Error registering exact alarm receiver:" + e.getMessage(), e);
        }
    }

    private static void registerAccessibilityListener(final Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        try {
            AccessibilityManager am = (AccessibilityManager)
                    ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am == null) return;
            am.addAccessibilityServicesStateChangeListener(
                    new AccessibilityManager.AccessibilityServicesStateChangeListener() {
                        @Override
                        public void onAccessibilityServicesStateChanged(AccessibilityManager manager) {
                            PreyLogger.d("accessibility services changed");
                            PermissionsReporter.sendIfChanged(ctx);
                        }
                    });
        } catch (Exception e) {
            PreyLogger.e("Error registering accessibility listener:" + e.getMessage(), e);
        }
    }
}
