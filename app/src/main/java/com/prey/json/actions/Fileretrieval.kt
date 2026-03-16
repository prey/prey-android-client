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
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.*
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
class Fileretrieval : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    companion object {
        private const val TARGET = "fileretrieval"
    }

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> scope.launch { start(context, options) }
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
    suspend fun start(context: Context, options: JSONObject) {
        val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
        val jobId = options.optString(PreyConfig.JOB_ID, null)
        val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
        try {
            PreyLogger.d("Fileretrieval started")
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STARTED, reason, messageId, "processed")
            val path = options.optString("path")
            val fileId = options.optString("file_id")
            if (fileId.isNullOrBlank() || fileId == "null") {
                throw IllegalArgumentException("file_id is missing or invalid")
            }
            val file = File(Environment.getExternalStorageDirectory(), path)
            if (!file.exists()) {
                throw NoSuchFileException(file, reason = "File not found at path")
            }
            val datasource = FileretrievalDatasource(context)
            val fileDto = FileretrievalDto().apply {
                setFileId(fileId)
                setPath(path)
                setSize(file.length())
                setStatus(0)
            }
            datasource.createFileretrieval(fileDto)
            PreyLogger.d("Fileretrieval starting upload: $fileId")
            val responseCode = PreyConfig.getPreyConfig(context).webServices.uploadFile(context, file, fileId, 0)
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                datasource.deleteFileretrieval(fileId)
                PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STOPPED, reason)
                PreyLogger.d("Fileretrieval completed successfully")
            } else {
                throw Exception("Server returned code: $responseCode")
            }
        } catch (e: Exception) {
            PreyLogger.e("Fileretrieval failed: ${e.message}", e)
            val reason = e.message ?: "Unknown error"
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_FAILED, reason)
        }
    }

}