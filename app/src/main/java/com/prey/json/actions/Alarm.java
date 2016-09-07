/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.actions.HttpDataService;
import com.prey.actions.alarm.AlarmThread;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class Alarm extends JsonAction {

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String sound = null;
        try {
            sound = parameters.getString("sound");
        } catch (Exception e) {
        }
        String messageId = null;
        try {
            messageId = parameters.getString(PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:"+messageId);
        } catch (Exception e) {
        }
        new AlarmThread(ctx, sound,messageId).start();
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject options) {
        PreyStatus.getInstance().setAlarmStop();
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        this.start(ctx, list, parameters);
    }

}
