/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.backwardcompatibility.AboveCupcakeSupport;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;

import org.json.JSONObject;

import java.util.List;

public class SyncName {

    public void start(final Context ctx, List<ActionResult> list, JSONObject parameters) {
        new Thread() {
            public void run() {
                try {
                    String newName = "";
                    String name = null;
                    try {
                        name = Settings.Secure.getString(ctx.getContentResolver(), "bluetooth_name");
                    } catch (Exception e) {
                    }
                    if (name != null && !"".equals(name)) {
                        newName = name;
                    } else {
                        String model = Build.MODEL;
                        String vendor = "Google";
                        try {
                            vendor = AboveCupcakeSupport.getDeviceVendor();
                        } catch (Exception e) {
                        }
                        newName = vendor + " " + model;
                    }
                    PreyLogger.d("new_name:" + newName);
                    JSONObject info = new JSONObject();
                    info.put("new_name", newName);
                    Event event = new Event(Event.DEVICE_RENAMED, info.toString());
                    new EventManagerRunner(ctx, event).run();
                } catch (Exception e) {
                }
            }
        }.start();
    }
}
