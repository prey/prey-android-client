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
import com.prey.actions.observer.ActionResult
import com.prey.actions.tree.TreeAction
import com.prey.json.UtilJson
import org.json.JSONObject

/**
 * Tree class responsible for handling file system operations.
 */
class Tree {

    /**
     * Retrieves a list of files and directories based on the provided parameters.
     *
     * @param context Context of the application.
     * @param actionResults List of ActionResult objects.
     * @param parameters JSONObject containing the parameters for the operation.
     */
    fun get(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject) {
        // Initialize variables to store message ID and reason.
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:${jobId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var depth = 1
        try {
            depth = parameters.getString("depth").toInt()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var path = parameters.getString("path")
        if ("sdcard" == path) {
            path = "/"
        }
        TreeAction().start(context, messageId, jobId, path, depth)
    }

}