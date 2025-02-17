/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net

import com.prey.PreyLogger

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * Represents an HTTP response, encapsulating the status code, response string, and header fields.
 */
class PreyHttpResponse {
    private var statusCode = 0
    private var responseAsString: String? = null
    private var response: HttpURLConnection? = null
    private var mapHeaderFields: Map<String, List<String>>? = null

    /**
     * Constructs a PreyHttpResponse object from a HttpURLConnection object.
     *
     * @param connection the HttpURLConnection object to construct from
     */
    constructor(connection: HttpURLConnection) {
        try {
            this.response = connection
            this.statusCode = connection.responseCode
            val inputStream = if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
            this.responseAsString = convertStreamToString(inputStream)
            PreyLogger.d("responseAsString:$responseAsString")
        } catch (e: IOException) {
            PreyLogger.d("Can't receive body stream from http connection, setting response string as ''")
            this.responseAsString = ""
        }
    }

    /**
     * Constructs a PreyHttpResponse object with a specified status code and response string.
     *
     * @param statusCode the status code of the HTTP response
     * @param responseAsString the response string of the HTTP response
     */
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

    /**
     * Constructs a PreyHttpResponse object with a specified status code, response string, and header fields.
     *
     * @param statusCode the status code of the HTTP response
     * @param responseAsString the response string of the HTTP response
     * @param mapHeaderFields the map of header fields
     */
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

    /**
     * Converts an input stream to a string.
     *
     * @param inputStream the input stream to convert
     * @return the converted string, or null if an exception occurs
     */
    private fun convertStreamToString(inputStream: InputStream?): String? {
        var out: String? = null
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            val response = StringBuffer()
            while ((reader.readLine().also { line = it }) != null) {
                response.append(line)
                response.append('\r')
            }
            reader.close()
            return response.toString()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
            null
        }
    }

    /**
     * Returns the status code of the HTTP response.
     *
     * @return the status code
     */
    fun getStatusCode(): Int {
        return statusCode
    }

    /**
     * Returns the response string of the HTTP response.
     *
     * @return the response string
     */
    fun getResponseAsString(): String? {
        return responseAsString
    }

    /**
     * Returns a string representation of the HTTP response.
     *
     * @return a string in the format "statusCode responseString"
     */
    override fun toString(): String {
        return "$statusCode $responseAsString"
    }

    /**
     * Returns the underlying HttpURLConnection object.
     *
     * @return the HttpURLConnection object, or null if not set
     */
    fun getResponse(): HttpURLConnection? {
        return response
    }

    /**
     * Sets the underlying HttpURLConnection object.
     *
     * @param response the HttpURLConnection object to set
     */
    fun setResponse(response: HttpURLConnection?) {
        this.response = response
    }

    /**
     * Returns the map of header fields from the HTTP response.
     *
     * @return a map of header fields, or null if not set
     */
    fun getMapHeaderFields(): Map<String, List<String>>? {
        return mapHeaderFields
    }
}