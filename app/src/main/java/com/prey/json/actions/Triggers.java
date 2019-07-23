/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.triggers.TriggerController;

import org.json.JSONObject;

import java.util.List;

public class Triggers {

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("starting Triggers");
        TriggerController.getInstance().run(ctx);
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.d("stop Triggers");
        TriggerController.getInstance().run(ctx);
    }
}