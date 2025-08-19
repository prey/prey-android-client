package com.prey.actions.fileretrieval

import android.content.Context
import android.os.Environment
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.json.UtilJson
import java.io.File
import java.net.HttpURLConnection

class FileretrievalAction {

    fun start(
        context: Context,
        messageId: String?,
        jobId: String?,
        path: String?,
        fileId: String?,
    ) {
        try {
            var responseCode = 0
            PreyLogger.d("Fileretrieval started")
            var reason: String? = null
            if (jobId != null && "" != jobId) {
                reason = "{\"device_job_id\":\"$jobId\"}"
            }
            // Send a notification to the server
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context, "processed",
                messageId, UtilJson.makeMapParam("start", "fileretrieval", "started", reason)
            )
            // Get the path and fileId from the parameters
            if (fileId.isNullOrEmpty()) {
                throw Exception("file_id null")
            }
            val filePath = "${Environment.getExternalStorageDirectory().toString()}/$path"
            // Create a file object
            val file = File(filePath)
            if (!file.exists()) {
                throw Exception("file not exists $filePath")
            }
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
            responseCode = PreyConfig.getInstance(context).getWebServices()
                .uploadFile(context, file, fileId, 0)
            PreyLogger.d("Fileretrieval responseCode uploadFile :${responseCode}")
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                datasource.deleteFileretrieval(fileId)
            }
            // If the response code is OK or CREATED, delete the fileretrieval
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context, "processed",
                messageId, UtilJson.makeMapParam("start", "fileretrieval", "stopped", reason)
            )
            PreyLogger.d("Fileretrieval stopped")
        } catch (e: Exception) {
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context, "failed",
                messageId, UtilJson.makeMapParam("start", "fileretrieval", "failed", e.message)
            )
            PreyLogger.d("Fileretrieval failed:${e.message}")
        }
    }

}