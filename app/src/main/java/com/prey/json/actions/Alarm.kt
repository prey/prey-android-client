/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.actions.alarm.AlarmThread
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyStatus
import org.json.JSONObject

class Alarm   {

    fun start(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        var sound: String? = null
        try {
            sound = UtilJson.getString(parameters, "sound")
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:$messageId")
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:$jobId")
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        AlarmThread(ctx, sound!!, messageId, jobId).start()
    }

    fun stop(ctx: Context?, list: List<ActionResult?>?, options: JSONObject?) {
        PreyStatus.getInstance().setAlarmStop()
    }


}