/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.Environment
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.fileretrieval.FileretrievalDatasource
import com.prey.actions.fileretrieval.FileretrievalDto
import com.prey.json.CommandTarget
import com.prey.json.UtilJson
import com.prey.net.PreyWebServices
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

/**
 * A [CommandTarget] responsible for handling the file retrieval action.
 *
 * This class receives commands from the Prey server to locate and upload a specific file
 * from the device's external storage. It manages the entire lifecycle of the file retrieval process,
 * including notifying the server of the action's status (started, stopped, failed) and
 * handling the file upload.
 *
 * The primary command handled is "start", which initiates the process.
 */
class Fileretrieval : CommandTarget {

    override fun execute(context: Context, command: String, options: JSONObject): Any? {
        return when (command) {
            "start" -> start(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Starts the file retrieval process.
     *
     * This function initiates the process of finding a specific file on the device's external storage
     * and uploading it to the Prey servers. It handles the entire lifecycle of the file retrieval action,
     * including sending status notifications (started, stopped, failed) to the backend.
     *
     * The process involves:
     * 1. Parsing necessary parameters like `message_id`, `job_id`, `path`, and `file_id` from the `options` JSON.
     * 2. Notifying the backend that the file retrieval action has started.
     * 3. Creating a local database record for the file retrieval task to handle potential interruptions.
     * 4. Attempting to upload the specified file.
     * 5. If the upload is successful, the local database record is deleted.
     * 6. Notifying the backend that the action has stopped (on success) or failed (on error).
     *
     * @param context The application context, used for accessing system services and the database.
     * @param options A [JSONObject] containing the parameters for the file retrieval action.
     *                Expected keys include "path" (the relative path to the file in external storage)
     *                and "file_id" (a unique identifier for the upload task). It may also contain
     *                "message_id" and "job_id" for tracking purposes.
     */
    fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Fileretrieval start options:${options}")
        var messageId: String? = null
        try {
            messageId = options.getString(PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var reason: String? = null
        try {
            val jobId = options.getString(PreyConfig.JOB_ID)
            reason = "{\"device_job_id\":\"${jobId}\"}"
            PreyLogger.d("jobId:${jobId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        try {
            PreyLogger.d("Fileretrieval started")
            CoroutineScope(Dispatchers.IO).launch {
                PreyWebServicesKt.sendNotifyActions(
                    context,
                    UtilJson.makeJsonResponse("start", "fileretrieval", "started", reason),
                    messageId,
                    "processed",
                )
            }
            val path: String = options.getString("path")
            val fileId: String = options.getString("file_id")
            if (fileId == null || "" == fileId || "null" == fileId) {
                throw Exception("file_id null")
            }
            val file = File("${Environment.getExternalStorageDirectory().toString()}/${path}")
            val fileDto = FileretrievalDto()
            fileDto.setFileId(fileId)
            fileDto.setPath(path)
            fileDto.setSize(file.length())
            fileDto.setStatus(0)
            val datasource = FileretrievalDatasource(context)
            datasource.createFileretrieval(fileDto)
            PreyLogger.d("Fileretrieval started uploadFile")
            val responseCode = PreyWebServices.getInstance().uploadFile(context, file, fileId, 0)
            PreyLogger.d("Fileretrieval responseCode uploadFile :${responseCode}")
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                datasource.deleteFileretrieval(fileId)
            }
            CoroutineScope(Dispatchers.IO).launch {
                PreyWebServicesKt.sendNotifyActions(
                    context,
                    UtilJson.makeJsonResponse("start", "fileretrieval", "stopped", reason),
                )
            }
            PreyLogger.d("Fileretrieval stopped")
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.IO).launch {
                PreyWebServicesKt.sendNotifyActions(
                    context,
                    UtilJson.makeJsonResponse("start", "fileretrieval", "failed", e.message),
                )
            }
            PreyLogger.e("Fileretrieval failed:${e.message}", e)
        }
    }

}