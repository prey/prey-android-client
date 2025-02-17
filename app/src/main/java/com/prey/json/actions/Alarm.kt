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
import com.prey.PreyConfig
import com.prey.PreyStatus
import org.json.JSONObject

/**
 * Alarm class responsible for starting and stopping alarm functionality.
 */
class Alarm   {

    /**
     * Starts the alarm with the given parameters.
     *
     * @param context The application context.
     * @param actionResults List of action results.
     * @param parameters JSONObject containing alarm parameters (sound, message ID, job ID).
     */
    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        // Extract sound, message ID, and job ID from parameters
        val sound = parameters?.getString("sound")
        val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
        val jobId = parameters?.getString(PreyConfig.JOB_ID)
        // Start the alarm thread with the extracted parameters
        AlarmThread(context, sound ?: "", messageId, jobId).start()
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
        PreyStatus.getInstance().setAlarmStop()
    }

}