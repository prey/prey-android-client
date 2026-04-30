/*******************************************************************************
 * Created by Pato Jofre
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PermissionsReporter;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;

import org.json.JSONObject;

import java.util.List;

public class ListPermission {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("started");
        try {
            PermissionsReporter.sendNow(ctx);
            PreyLogger.d("stopped");
        } catch (Exception e) {
            PreyLogger.e("Error:" + e.getMessage(), e);
        }
    }
}
