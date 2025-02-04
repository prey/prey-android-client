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

class FileretrievalController private constructor() {
    fun run(ctx: Context) {
        PreyLogger.d("______________ FileretrievalController run _____________________")
        var connect = false
        var j = 0
        do {
            connect =
                (PreyConfig.getInstance(ctx).isConnectionExists() || PreyWifiManager.getInstance()
                    .isOnline(ctx))
            PreyLogger.d("______________ FileretrievalController connect:+$connect")
            if (connect) {
                break
            } else {
                try {
                    Thread.sleep(4000)
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            j++
        } while (j < 5)
        if (connect) {
            val datasource = FileretrievalDatasource(ctx)
            val list: List<FileretrievalDto> = datasource.allFileretrieval()
            var i = 0
            while (list != null && i < list.size) {
                val dto = list[i]
                val fileId = dto.getFileId()
                PreyLogger.d("id:" + dto.getFileId() + " " + dto.getPath())
                try {
                    val dtoStatus = PreyWebServices.getInstance().uploadStatus(
                        ctx!!, fileId
                    )
                    PreyLogger.d("dtoStatus:" + dtoStatus!!.getStatus())
                    if (dtoStatus.getStatus() == 1) {
                        datasource.deleteFileretrieval(fileId)
                    }
                    if (dtoStatus.getStatus() == 2 || dtoStatus.getStatus() == 0) {
                        val total = dtoStatus.getTotal()
                        val file = File(
                            Environment.getExternalStorageDirectory()
                                .toString() + "/" + dto.getPath()
                        )
                        PreyLogger.d("total:" + total + " size:" + dtoStatus.getSize() + " length:" + file.length())
                        val responseCode =
                            PreyWebServices.getInstance().uploadFile(ctx, file, fileId, total)
                        PreyLogger.d("responseCode:$responseCode")
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                            datasource.deleteFileretrieval(fileId)
                        }
                    }
                    if (dtoStatus.getStatus() == 404) {
                        datasource.deleteFileretrieval(fileId)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("FileretrievalController Error:" + e.message, e)
                }
                i++
            }
        }
    }

    fun deleteAll(ctx: Context?) {
        val datasource = FileretrievalDatasource(ctx)
        datasource.deleteAllFileretrieval()
    }

    companion object {
        private var instance: FileretrievalController? = null
        fun getInstance(): FileretrievalController {
            if (instance == null) {
                instance = FileretrievalController()
            }
            return instance!!
        }


    }
}