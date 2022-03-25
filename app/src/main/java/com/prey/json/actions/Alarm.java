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
import com.prey.json.UtilJson;

public class Alarm extends JsonAction {

    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
        String sound = null;
        try {
            sound = UtilJson.getString(parameters, "sound");
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s" , e.getMessage()), e);
        }
        String messageId = null;
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID);
            PreyLogger.d("messageId:" + messageId);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s" , e.getMessage()), e);
        }
        String jobId = null;
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID);
            PreyLogger.d("jobId:" + jobId);
        } catch (Exception e) {
            PreyLogger.e(String.format("Error:%s" , e.getMessage()), e);
        }
        new AlarmThread(ctx, sound, messageId, jobId).start();
    }

    public void stop(Context ctx, List<ActionResult> list, JSONObject options) {
        PreyStatus.getInstance().setAlarmStop();
    }

    public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
        this.start(ctx, list, parameters);
    }

}