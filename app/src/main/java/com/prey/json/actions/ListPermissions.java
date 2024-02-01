/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.events.Event;
import com.prey.events.manager.EventThread;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.List;

public class ListPermissions {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        get(ctx, list, parameters);
    }

    /**
     * Method collects all permissions
     *
     * @param ctx
     * @param list
     * @param parameters
     */
    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String messageId = null;
        String reason = null;
        try {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "list_permissions", "started", reason));
            boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(ctx);
            boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(ctx);
            boolean canAccessCamera = PreyPermission.canAccessCamera(ctx);
            boolean canAccessStorage = PreyPermission.canAccessStorage(ctx);
            boolean canAccessBackgroundLocation = PreyPermission.canAccessBackgroundLocationView(ctx);
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(ctx);
            boolean canAccessibility = PreyPermission.isAccessibilityServiceView(ctx);
            boolean isAdminActive = FroyoSupport.getInstance(ctx).isAdminActive();
            boolean notification = !PreyPermission.areNotificationsEnabled(ctx);
            JSONObject info = new JSONObject();
            info.put("location", (canAccessFineLocation || canAccessCoarseLocation));
            info.put("location_background", canAccessBackgroundLocation);
            info.put("camera", canAccessCamera);
            info.put("storage", canAccessStorage);
            info.put("admin", isAdminActive);
            info.put("draw_overlays", canDrawOverlays);
            info.put("accessibility", canAccessibility);
            info.put("notification", notification);
            PreyLogger.d(String.format("list_permission:%s", info.toString()));
            Event event = new Event();
            event.setName("list_permission");
            event.setInfo(info.toString());
            JSONObject jsonObjectStatus = new JSONObject();
            PreyLogger.d(info.toString());
            new EventThread(ctx, event, jsonObjectStatus).start();
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "processed", messageId, UtilJson.makeMapParam("start", "list_permissions", "stopped", reason));
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, "failed", messageId, UtilJson.makeMapParam("start", "list_permissions", "failed", e.toString()));
        }
    }

}