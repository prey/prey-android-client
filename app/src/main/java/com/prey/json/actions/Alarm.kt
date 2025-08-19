/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyStatus
import com.prey.actions.alarm.Alarm
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import org.json.JSONObject

/**
 * Alarm class responsible for starting and stopping alarm functionality.
 */
class Alarm {


    /**
     * Starts the alarm with the given parameters.
     *
     * @param context The application context.
     * @param actionResults List of action results.
     * @param parameters JSONObject containing alarm parameters (sound, message ID, job ID).
     */
    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        // Extract sound, message ID, and job ID from parameters
        var sound: String? = null
        try {
            sound = UtilJson.getStringValue(parameters, "sound")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:$messageId")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:$jobId")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        // Start the alarm thread with the extracted parameters
        Alarm(context, sound ?: "", messageId, jobId).start()
    }

    /**
     * Stops the alarm.
     *
     * @param context The application context.
     * @param actionResults List of action results.
     * @param parameters JSONObject containing alarm parameters (not used in this implementation).
     */
    fun stop(context: Context?, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        // Set alarm stop status using PreyStatus instance
        PreyStatus.getInstance().setStateOfAlarm(PreyStatus.AlarmState.FINISH)
    }

}