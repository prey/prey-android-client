/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;

import com.prey.PreyLogger;
import com.prey.actions.alert.AlertConfig;
import com.prey.actions.alert.AlertThread;
import com.prey.actions.observer.ActionJob;

public class PopUpAlertAction extends PreyAction {

    public static final String DATA_ID = "alert";
    public final String ID = "alert";

    public HttpDataService run(Context ctx) {
        return null;
    }

    @Override
    public String textToNotifyUserOnEachReport(Context ctx) {
        return "";
    }

    @Override
    public void execute(ActionJob actionJob, Context ctx) {
        Bundle bundle = new Bundle();
        for (Iterator<Map.Entry<String, String>> it = getConfig().entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            bundle.putString(entry.getKey(), entry.getValue());
        }
        String message = bundle.getString("alert_message");
        if (message == null) {
            message = bundle.getString("message");
        }
        new AlertThread(ctx, message, null, null, true).fullscreen(AlertConfig.getAlertConfig(ctx).getNotificationId());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
    }

    @Override
    public boolean isSyncAction() {
        return false;
    }

    @Override
    public boolean shouldNotify() {
        return false;
    }

    public int getPriority(){
        return POPUPALERT_PRIORITY;
    }

}
