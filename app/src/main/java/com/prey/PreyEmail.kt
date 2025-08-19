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

/**
 * Object responsible for sending email data.
 */
object PreyEmail {

    /**
     * Sends email data to the server.
     *
     * @param context Context of the application.
     * @param data HttpDataService containing the data to be sent.
     */
    fun sendDataMail(context: Context, data: HttpDataService?) {
        try {
            if (data != null) {
                val entityFiles = data.getEntityFiles()
                if (entityFiles != null && entityFiles.size >= 0) {
                    val preyConfig = PreyConfig.getInstance(context)
                    val url =
                        preyConfig.getWebServices().getFileUrlJson(context)
                    PreyLogger.d("URL:$url")
                    val parameters: MutableMap<String, String?> = HashMap()
                    var preyHttpResponse: PreyHttpResponse? = null
                    preyHttpResponse = PreyRestHttpClient.getInstance(context)
                        .postAutentication(url, parameters, entityFiles)
                    PreyLogger.d("status line:${preyHttpResponse!!.getStatusCode()}")
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

}