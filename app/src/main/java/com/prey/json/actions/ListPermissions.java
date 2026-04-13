/*******************************************************************************
 * Created by Prey Inc.
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.List;

public class ListPermissions {

    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        String reason = null;
        try {
            String jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID);
            if (jobId != null && !"".equals(jobId)) {
                reason = "{\"device_job_id\":\"" + jobId + "\"}";
            }
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
        }
        try {
            PreyLogger.d("ListPermissions started");
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId,
                    UtilJson.makeMapParam("get", "list_permissions", "started", reason));

            sendPermissions(ctx);

            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,
                    UtilJson.makeMapParam("get", "list_permissions", "stopped", reason));
            PreyLogger.d("ListPermissions stopped");
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, messageId,
                    UtilJson.makeMapParam("get", "list_permissions", "failed", e.getMessage()));
            PreyLogger.d("ListPermissions failed:" + e.getMessage());
        }
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        get(ctx, list, parameters);
    }

    /**
     * Sends the current permission status to the backend as a list_permission event.
     * Can be called from any context (MDM registration, manual signup, or backend command).
     */
    public static void sendPermissions(Context ctx) {
        try {
            JSONObject permissionInfo = getPermissions(ctx);
            PreyLogger.d(String.format("ListPermissions sendPermissions: %s", permissionInfo.toString()));
            PreyWebServices.getInstance().sendPreyHttpEvent(ctx,
                    new Event("list_permission", permissionInfo.toString()),
                    new JSONObject());
        } catch (Exception e) {
            PreyLogger.e(String.format("Error sendPermissions: %s", e.getMessage()), e);
        }
    }

    /**
     * Collects the current status of all permissions.
     * Package-visible for testing.
     */
    static JSONObject getPermissions(Context ctx) throws Exception {
        boolean location = PreyPermission.canAccessFineLocation(ctx) || PreyPermission.canAccessCoarseLocation(ctx);
        boolean locationBackground = PreyPermission.canAccessBackgroundLocation(ctx);
        boolean camera = PreyPermission.canAccessCamera(ctx);
        boolean notification = PreyPermission.areNotificationsEnabled(ctx);
        boolean storage = PreyPermission.canAccessStorage(ctx);
        boolean drawOverlays = PreyPermission.canDrawOverlays(ctx);
        boolean admin = FroyoSupport.getInstance(ctx).isAdminActive();
        boolean accessibility = PreyPermission.isAccessibilityServiceEnabled(ctx);

        JSONObject json = new JSONObject();
        json.put("location", String.valueOf(location));
        json.put("location_background", String.valueOf(locationBackground));
        json.put("camera", String.valueOf(camera));
        json.put("notification", String.valueOf(notification));
        json.put("storage", String.valueOf(storage));
        json.put("draw_overlays", String.valueOf(drawOverlays));
        json.put("admin", String.valueOf(admin));
        json.put("accessibility", String.valueOf(accessibility));
        return json;
    }
}
