/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.content.Context
import com.prey.actions.HttpDataService
import com.prey.net.PreyHttpResponse
import com.prey.net.PreyRestHttpClient
import com.prey.net.PreyWebServices

object PreyEmail {
    fun sendDataMail(ctx: Context, data: HttpDataService?) {
        try {
            if (data != null) {
                val entityFiles = data.getEntityFiles()
                if (entityFiles != null && entityFiles.size >= 0) {
                    val url = PreyWebServices.getInstance().getFileUrlJson(ctx)
                    PreyLogger.d("URL:$url")
                    val parameters: MutableMap<String, String?> = HashMap()
                    val preyConfig = PreyConfig.getInstance(ctx)
                    var preyHttpResponse: PreyHttpResponse? = null
                    preyHttpResponse = PreyRestHttpClient.getInstance(ctx)
                        .postAutentication(url, parameters, entityFiles)
                    PreyLogger.d("status line:" + preyHttpResponse!!.getStatusCode())
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error causa:" + e.message + e.message, e)
        }
    }

}