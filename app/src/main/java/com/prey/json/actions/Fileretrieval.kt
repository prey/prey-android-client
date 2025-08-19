/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context

import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.fileretrieval.FileretrievalAction

import org.json.JSONObject

/**
 * This class is responsible for handling the fileretrieval process.
 * It takes a context, a list of action results, and a JSONObject as parameters.
 * It starts the fileretrieval process and handles the response.
 */
class Fileretrieval {

    /**
     * This function starts the fileretrieval process.
     * @param context: The application context.
     * @param actionResults: A list of action results.
     * @param parameters: A JSONObject containing parameters.
     */
    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        // Initialize response code and messageId
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:${jobId}")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        val path = parameters?.getString("path")
        val fileId = parameters?.getString("file_id")
        FileretrievalAction().start(context, messageId, jobId, path, fileId)
    }

}