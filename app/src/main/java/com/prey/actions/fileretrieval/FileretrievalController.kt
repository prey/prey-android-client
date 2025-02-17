/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.fileretrieval

import android.content.Context
import android.os.Environment

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.managers.PreyWifiManager
import com.prey.net.PreyWebServices
import java.io.File
import java.net.HttpURLConnection

/**
 * This class is responsible for controlling the file retrieval process.
 * It checks for internet connection, retrieves a list of files to upload,
 * and uploads the files to the server.
 */
class FileretrievalController private constructor() {

    /**
     * Runs the file retrieval process.
     * @param context The application context.
     */
    fun run(context: Context) {
        PreyLogger.d("______________ FileretrievalController run _____________________")
        var connect = false
        var j = 0
        do {
            connect =
                (PreyConfig.getInstance(context)
                    .isConnectionExists() || PreyWifiManager.getInstance()
                    .isOnline(context))
            PreyLogger.d("______________ FileretrievalController connect:${connect}")
            if (connect) {
                break
            } else {
                Thread.sleep(4000)
            }
            j++
        } while (j < 5)
        if (connect) {
            val datasource = FileretrievalDatasource(context)
            val list: List<FileretrievalDto> = datasource.allFileretrieval()
            var i = 0
            while (list != null && i < list.size) {
                val dto = list[i]
                val fileId = dto.getFileId()
                PreyLogger.d("id:${dto.getFileId()} ${dto.getPath()}")
                try {
                    val dtoStatus = PreyWebServices.getInstance().uploadStatus(
                        context, fileId
                    )
                    PreyLogger.d("dtoStatus:${dtoStatus!!.getStatus()}")
                    if (dtoStatus.getStatus() == 1) {
                        datasource.deleteFileretrieval(fileId)
                    }
                    if (dtoStatus.getStatus() == 2 || dtoStatus.getStatus() == 0) {
                        val total = dtoStatus.getTotal()
                        val file = File("${Environment.getExternalStorageDirectory()}/${dto.getPath()}")
                        PreyLogger.d("total:${total} size:${dtoStatus.getSize()} length:${file.length()}")
                        val responseCode =
                            PreyWebServices.getInstance().uploadFile(context, file, fileId, total)
                        PreyLogger.d("responseCode:$responseCode")
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                            datasource.deleteFileretrieval(fileId)
                        }
                    }
                    if (dtoStatus.getStatus() == 404) {
                        datasource.deleteFileretrieval(fileId)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("FileretrievalController Error:${e.message}", e)
                }
                i++
            }
        }
    }

    /**
     * Deletes all files from the data source.
     * @param context The application context.
     */
    fun deleteAll(context: Context?) {
        val datasource = FileretrievalDatasource(context)
        datasource.deleteAllFileretrieval()
    }

    companion object {
        private var instance: FileretrievalController? = null
        fun getInstance(): FileretrievalController {
            return instance ?: FileretrievalController().also { instance = it }
        }
    }
}