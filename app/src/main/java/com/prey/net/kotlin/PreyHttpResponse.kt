/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.kotlin

import com.prey.kotlin.PreyLogger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection


class PreyHttpResponse {
    private var statusCode = 0
    private var responseAsString: String? = null
    private var response: HttpURLConnection? = null
    private var mapHeaderFields: Map<String, List<String>>? = null

    constructor(connection: HttpURLConnection) {
        try {
            this.response = connection
            this.statusCode = connection.responseCode
            var input: InputStream? = null
            input =
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
            this.responseAsString = convertStreamToString(input)
            PreyLogger.d("responseAsString:$responseAsString")
        } catch (e: IOException) {
            PreyLogger.d("Can't receive body stream from http connection, setting response string as ''")
            this.responseAsString = ""
        }
    }

    constructor(statusCode: Int, responseAsString: String) {
        try {
            this.response = null
            this.statusCode = statusCode
            this.responseAsString = responseAsString
            PreyLogger.d("responseAsString:$responseAsString")
        } catch (e: Exception) {
            PreyLogger.d("Can't receive body stream from http connection, setting response string as ''")
            this.responseAsString = ""
        }
    }

    constructor(
        statusCode: Int,
        responseAsString: String,
        mapHeaderFields: Map<String, List<String>>?
    ) {
        try {
            this.response = null
            this.statusCode = statusCode
            this.responseAsString = responseAsString
            this.mapHeaderFields = mapHeaderFields
            PreyLogger.d("responseAsString:$responseAsString")
        } catch (e: Exception) {
            PreyLogger.d("Can't receive body stream from http connection, setting response string as ''")
            this.responseAsString = ""
        }
    }

    private fun convertStreamToString(`is`: InputStream?): String? {
        var out: String? = null
        try {
            val rd = BufferedReader(InputStreamReader(`is`))
            var line: String?
            val response = StringBuffer()
            while ((rd.readLine().also { line = it }) != null) {
                response.append(line)
                response.append('\r')
            }
            rd.close()
            out = response.toString()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        return out
    }

    fun getStatusCode(): Int {
        return statusCode
    }

    fun getResponseAsString(): String? {
        return responseAsString
    }

    override fun toString(): String {
        return "$statusCode $responseAsString"
    }

    fun getResponse(): HttpURLConnection? {
        return response
    }

    fun setResponse(response: HttpURLConnection?) {
        this.response = response
    }

    fun getMapHeaderFields(): Map<String, List<String>>? {
        return mapHeaderFields
    }
}