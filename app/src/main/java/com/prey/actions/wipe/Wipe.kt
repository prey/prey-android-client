/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.wipe

import android.content.Context
import com.prey.PreyLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Responsible for wiping the device data.
 *
 * @param context The application context.
 * @param wipe Whether to wipe the device data.
 * @param deleteSD Whether to delete the SD card data.
 * @param messageId The message ID associated with the wipe action.
 * @param jobId The job ID associated with the wipe action.
 */
class Wipe(
    private val context: Context,
    private val wipe: Boolean,
    private val deleteSD: Boolean,
    private val messageId: String,
    private val jobId: String?
) {

    /**
     * Runs the wipe.
     */
    fun start() {
        PreyLogger.d("Wipe CoroutineScope_________")
        CoroutineScope(Dispatchers.IO).launch {
            WipeAction().start(context, messageId, jobId, wipe, deleteSD)
        }
    }

}