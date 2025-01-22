/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import android.os.Environment
import com.prey.actions.fileretrieval.kotlin.FileretrievalDatasource
import com.prey.actions.fileretrieval.kotlin.FileretrievalDto
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

class Fileretrieval {
    fun start(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject) {
        var responseCode = 0
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var reason: String? = null
        try {
            val jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d(String.format("jobId:%s", jobId))
            if (jobId != null && "" != jobId) {
                reason = "{\"device_job_id\":\"$jobId\"}"
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        try {
            PreyLogger.d("Fileretrieval started")
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                "processed",
                messageId,
                UtilJson.makeMapParam("start", "fileretrieval", "started", reason)
            )
            val path = parameters.getString("path")
            val fileId = parameters.getString("file_id")
            if (fileId == null || "" == fileId || "null" == fileId) {
                throw Exception("file_id null")
            }
            val file = File(Environment.getExternalStorageDirectory().toString() + "/" + path)
            val fileDto = FileretrievalDto()
            fileDto.setFileId (fileId)
            fileDto.setPath ( path)
            fileDto.setSize ( file.length())
            fileDto.setStatus ( 0)

            val datasource = FileretrievalDatasource(ctx)
            datasource.createFileretrieval(fileDto)
            PreyLogger.d("Fileretrieval started uploadFile")
            responseCode = PreyWebServices.getInstance().uploadFile(ctx, file, fileId, 0)
            PreyLogger.d(String.format("Fileretrieval responseCode uploadFile :%d", responseCode))
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                datasource.deleteFileretrieval(fileId)
            }
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "fileretrieval", "stopped", reason)
            )
            PreyLogger.d("Fileretrieval stopped")
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                messageId,
                UtilJson.makeMapParam("start", "fileretrieval", "failed", e.message)
            )
            PreyLogger.d(String.format("Fileretrieval failed:%s", e.message))
        }
    }
}