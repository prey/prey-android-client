/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.Environment

import com.prey.actions.fileretrieval.FileretrievalDatasource
import com.prey.actions.fileretrieval.FileretrievalDto
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices

import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

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
        var responseCode = 0
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var reason: String? = null
        try {
            val jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:${jobId}")
            if (jobId != null && "" != jobId) {
                reason = "{\"device_job_id\":\"$jobId\"}"
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        try {
            PreyLogger.d("Fileretrieval started")
            // Send a notification to the server
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                "processed",
                messageId,
                UtilJson.makeMapParam("start", "fileretrieval", "started", reason)
            )
            // Get the path and fileId from the parameters
            val path = parameters?.getString("path")
            val fileId = parameters?.getString("file_id")
            if (fileId.isNullOrEmpty()) {
                throw Exception("file_id null")
            }
            // Create a file object
            val file = File("${Environment.getExternalStorageDirectory().toString()}/$path")
            // Create a FileretrievalDto object and set its properties
            val fileDto = FileretrievalDto().apply {
                this.setFileId(fileId)
                this.setPath(path)
                this.setSize(file.length())
                this.setStatus(0)
            }
            // Create a FileretrievalDatasource object
            val datasource = FileretrievalDatasource(context)
            datasource.createFileretrieval(fileDto)
            // Upload the file
            PreyLogger.d("Fileretrieval started uploadFile")
            responseCode = PreyWebServices.getInstance().uploadFile(context, file, fileId, 0)
            PreyLogger.d("Fileretrieval responseCode uploadFile :${responseCode}")
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                datasource.deleteFileretrieval(fileId)
            }
            // If the response code is OK or CREATED, delete the fileretrieval
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "fileretrieval", "stopped", reason)
            )
            PreyLogger.d("Fileretrieval stopped")
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                messageId,
                UtilJson.makeMapParam("start", "fileretrieval", "failed", e.message)
            )
            PreyLogger.d("Fileretrieval failed:${e.message}")
        }
    }
}