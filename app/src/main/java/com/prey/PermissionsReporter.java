/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.content.Context;
import android.os.Build;

import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

public class PermissionsReporter {

    private static final String EVENT_NAME = "list_permission";
    private static final String ACTION_TARGET = "list_permission";

    private PermissionsReporter() {}

    /**
     * Spontaneous send: app start, listener fires, onResume, etc. Sends ONLY
     * the event to /events; no start/stop wrapping at /response (there is no
     * server-issued action to report the lifecycle of). Diffs against the
     * last stored snapshot to avoid redundant sends.
     */
    public static void sendIfChanged(Context ctx) {
        if (ctx == null) return;
        String current = computeSnapshotJson(ctx);
        if (current == null) return;
        String previous = PreyConfig.getPreyConfig(ctx).getLastPermissionsSnapshot();
        if (current.equals(previous)) return;
        PreyConfig.getPreyConfig(ctx).setLastPermissionsSnapshot(current);
        dispatchEvent(ctx, current);
    }

    /**
     * Server-on-demand send: the panel sent a list_permission action and we
     * must report its lifecycle (started → event → stopped) to /response in
     * addition to the event itself. Always sends fresh, no diff.
     */
    public static void sendNow(Context ctx) {
        if (ctx == null) return;
        String current = computeSnapshotJson(ctx);
        if (current == null) return;
        PreyConfig.getPreyConfig(ctx).setLastPermissionsSnapshot(current);
        dispatchActionWithEvent(ctx, current);
    }

    private static String computeSnapshotJson(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null;
        boolean skipManual = PreyConfig.getPreyConfig(ctx).isMdmSkipManualPermissions();
        try {
            JSONObject info = new JSONObject();
            info.put("fineLocation", PreyPermission.canAccessFineLocation(ctx));
            info.put("coarseLocation", PreyPermission.canAccessCoarseLocation(ctx));
            info.put("camera", PreyPermission.canAccessCamera(ctx));
            info.put("storage", PreyPermission.canAccessStorage(ctx));
            info.put("backgroundLocation", PreyPermission.canAccessBackgroundLocationView(ctx));
            info.put("exactAlarms", PreyPermission.canScheduleExactAlarms(ctx));
            info.put("drawOverlays", skipManual || PreyPermission.canDrawOverlays(ctx));
            info.put("accessibility", skipManual || PreyPermission.isAccessibilityServiceView(ctx));
            info.put("adminActive", skipManual || FroyoSupport.getInstance(ctx).isAdminActive());
            info.put("externalStorageManager", PreyPermission.isExternalStorageManagerView(ctx));
            return info.toString();
        } catch (Exception e) {
            PreyLogger.e("Error building info:" + e.getMessage(), e);
            return null;
        }
    }

    private static void dispatchEvent(Context ctx, final String infoJson) {
        final Context appCtx = ctx.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Event event = new Event(EVENT_NAME, infoJson);
                    PreyWebServices.getInstance().sendPreyHttpEvent(appCtx, event, new JSONObject());
                } catch (Exception e) {
                    PreyLogger.e("Error sending event:" + e.getMessage(), e);
                }
            }
        }).start();
    }

    private static void dispatchActionWithEvent(Context ctx, final String infoJson) {
        final Context appCtx = ctx.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                            appCtx, UtilJson.makeMapParam("start", ACTION_TARGET, "started"));
                    Event event = new Event(EVENT_NAME, infoJson);
                    PreyWebServices.getInstance().sendPreyHttpEvent(appCtx, event, new JSONObject());
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                            appCtx, UtilJson.makeMapParam("stop", ACTION_TARGET, "stopped"));
                } catch (Exception e) {
                    PreyLogger.e("Error sending event:" + e.getMessage(), e);
                }
            }
        }).start();
    }
}
