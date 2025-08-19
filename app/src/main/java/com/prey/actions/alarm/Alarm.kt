/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alarm

import android.content.Context

import com.prey.PreyLogger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * That plays an alarm sound and handles its lifecycle.
 *
 * @param context The application context.
 * @param soundType The type of sound to play (e.g. "alarm", "ring", "modem", etc.).
 * @param messageId The message ID associated with the alarm.
 * @param jobId The job ID associated with the alarm.
 */
class Alarm(
    private val context: Context,
    private val soundType: String,
    private val messageId: String?,
    private val jobId: String?
) {
    private val alarmAction = AlarmAction()

    /**
     * Runs the alarm.
     *
     * This method starts the alarm using the {@link AlarmAction} class.
     */
    fun start() {
        PreyLogger.d("______Alarm CoroutineScope_________")
        CoroutineScope(Dispatchers.IO).launch {
            alarmAction.start(
                context,
                messageId,
                jobId,
                soundType
            )
        }
    }

}