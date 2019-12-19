/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyApp;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.json.JsonAction;
import com.prey.preferences.RunBackgroundCheckBoxPreference;

import org.json.JSONObject;

import java.util.List;

public class UserActivated  extends JsonAction {

    @Override
    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.i("UserActivated");
        PreyConfig.getPreyConfig(ctx).setInstallationStatus("OK");
        try {
            RunBackgroundCheckBoxPreference.notifyReady(ctx);
            new PreyApp().run(ctx);
        } catch (Exception e) {
        }
        Intent intent = new Intent(ctx, CheckPasswordHtmlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

}
