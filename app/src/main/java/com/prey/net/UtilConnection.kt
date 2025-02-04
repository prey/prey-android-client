/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net

import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Base64
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.net.http.EntityFile
import com.prey.net.http.SimpleMultipartEntity
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import javax.net.ssl.HttpsURLConnection

class UtilConnection {

    private fun getCredentials(user: String, password: String): String {
        return Base64.encodeToString(("$user:$password").toByteArray(), Base64.NO_WRAP)
    }

    private fun getUserAgent(context: Context): String {
        return "Prey/" + PreyConfig.getInstance(context)
            .getPreyVersion() + " (Android " + PreyUtils.getBuildVersionRelease() + ")"
    }

    fun getAuthorization(context: Context): String {
        return "Basic " + getCredentials(PreyConfig.getInstance(context).getApiKey()!!, "X")
    }

    private fun getAuthorization(user: String, pass: String): String {
        return "Basic " + getCredentials(user, pass)
    }

    @Throws(Exception::class)
    public fun connectionPut(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_PUT,
            contentType,
            null,
            null,
            null,
            null
        )
    }

    @Throws(Exception::class)
    fun connectionPutAuthorization(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_PUT,
            contentType,
            getAuthorization(mContext),
            null,
            null,
            null
        )
    }

    @Throws(Exception::class)
    fun connectionGet(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_GET,
            contentType,
            null,
            null,
            null,
            null
        )
    }

    @Throws(Exception::class)
    fun connectionGetAuthorization(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_GET,
            contentType,
            getAuthorization(mContext),
            null,
            null,
            null
        )
    }

    @Throws(Exception::class)
    fun connectionGetAuthorization(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String,
        user: String,
        pass: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_GET,
            contentType,
            getAuthorization(user, pass),
            null,
            null,
            null
        )
    }

    @Throws(Exception::class)
    fun connectionDelete(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_DELETE,
            contentType,
            null,
            null,
            null,
            null
        )
    }


    @Throws(java.lang.Exception::class)
    fun connectionDeleteAuthorization(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String?
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri, params, com.prey.net.UtilConnection.REQUEST_METHOD_DELETE,
            contentType!!, getAuthorization(mContext), null, null, null
        )
    }

    @Throws(Exception::class)
    fun connectionPost(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_POST,
            contentType,
            null,
            null,
            null,
            null
        )
    }

    @Throws(Exception::class)
    fun connectionPostAuthorization(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_POST,
            contentType,
            getAuthorization(mContext),
            null,
            null,
            null
        )
    }

    @Throws(java.lang.Exception::class)
    fun connectionPostAuthorization(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String?,
        entityFiles: List<EntityFile>?
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri, params, REQUEST_METHOD_POST,
            contentType!!, getAuthorization(mContext), null, entityFiles, null
        )
    }


    @Throws(java.lang.Exception::class)
    fun connectionPostAuthorizationStatus(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String?,
        status: String?
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri, params, com.prey.net.UtilConnection.REQUEST_METHOD_POST,
            contentType!!, getAuthorization(mContext), status, null, null
        )
    }


    @Throws(Exception::class)
    fun connectionPostAuthorizationCorrelationId(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String,
        status: String?,
        correlationId: String?
    ): PreyHttpResponse? {
        return connection(
            mContext,
            uri,
            params,
            REQUEST_METHOD_POST,
            contentType,
            getAuthorization(mContext),
            status,
            null,
            correlationId
        )
    }

    @Throws(java.lang.Exception::class)
    fun connection(
        mContext: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        requestMethod: String,
        contentType: String,
        authorization: String?,
        status: String?,
        entityFiles: List<EntityFile>?,
        correlationId: String?
    ): PreyHttpResponse? {
        var response: PreyHttpResponse? = null
        val url = URL(uri)
        var connection: HttpURLConnection? = null
        var retry = 0
        var delay = false
        if (params != null) {
            val ite = params.keys.iterator()
            while (ite.hasNext()) {
                val key = ite.next()
                PreyLogger.d("[" + key + "]:" + params[key])
            }
        }
        val multiple = SimpleMultipartEntity()
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmZ")
        val listOutputStream: MutableList<ByteArrayOutputStream> = ArrayList()
        try {
            do {
                if (delay) {
                    Thread.sleep((ARRAY_RETRY_DELAY_MS[retry] * 1000).toLong())
                }
                if (!isInternetAvailable()) {
                    PreyLogger.d("NET isInternetAvailable: $retry uri:$uri")
                    delay = true
                    retry++
                } else {
                    connection = if (uri.indexOf("https:") >= 0) {
                        url.openConnection() as HttpsURLConnection
                    } else {
                        url.openConnection() as HttpURLConnection
                    }
                    val out = ByteArrayOutputStream()
                    connection.requestMethod = requestMethod
                    connection.setRequestProperty("Accept", "*/*")
                    if (contentType != null) {
                        PreyLogger.d("Content-Type:$contentType")
                        connection.addRequestProperty("Content-Type", contentType)
                    }
                    if (authorization != null) {
                        connection.addRequestProperty("Authorization", authorization)
                        PreyLogger.d("Authorization:$authorization")
                    }
                    if (status != null) {
                        connection.addRequestProperty("X-Prey-Status", status)
                        PreyLogger.d("X-Prey-Status:$status")
                    }

                    if (correlationId != null) {
                        connection.addRequestProperty("X-Prey-Correlation-ID", correlationId)
                        PreyLogger.d("X-Prey-Correlation-ID:$correlationId")
                    }
                    val deviceId = PreyConfig.getInstance(mContext).getDeviceId()
                    if (deviceId != null) {
                        connection.addRequestProperty("X-Prey-Device-ID", deviceId)
                        PreyLogger.d("X-Prey-Device-ID:$deviceId")
                        connection.addRequestProperty("X-Prey-State", status)
                        PreyLogger.d("X-Prey-State:$status")
                    }

                    connection.addRequestProperty("User-Agent", getUserAgent(mContext))
                    PreyLogger.d("User-Agent:" + getUserAgent(mContext))
                    connection.addRequestProperty("Origin", "android:com.prey")
                    PreyLogger.d("REPORT params__________:"+params!!.size)
                    PreyLogger.d("REPORT entityFiles is null:"+(entityFiles==null))
                    if (entityFiles == null && (params != null && params.size > 0)) {
                        val os: OutputStream = connection.outputStream
                        val dos = DataOutputStream(os)
                        dos.writeBytes(getPostDataString(params))
                    }

                    if (entityFiles != null && entityFiles.size > 0) {
                        for ((key, value1) in params.entries) {
                            var value: String? = null
                            try {
                                value = value1
                            } catch (e: java.lang.Exception) {
                            }
                            if (value == null) {
                                value = ""
                            }
                            multiple.addPart(key, value)
                        }

                            var i = 0
                            while (entityFiles != null && i < entityFiles.size ) {
                                val entityFile = entityFiles[i]
                                var isLast = false
                                if((i + 1) == entityFiles.size){
                                    isLast = true
                                }
                                val outputStream = multiple.addPart(
                                    entityFile.getName()!!,
                                    entityFile.getFilename()!!,
                                    entityFile.getFile(),
                                    entityFile.getType()!!,
                                    isLast
                                )
                                listOutputStream.add(outputStream!!)
                                i++
                            }

                        connection.setRequestProperty("Content-Length", "" + multiple.contentLength)
                        connection.setRequestProperty("Content-Type", multiple.contentType)
                        val os = connection.outputStream
                        multiple.writeTo(os)
                    }


                    val policy = ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)

                    val responseCode = connection!!.responseCode
                    val responseMessage = connection!!.responseMessage
                    when (responseCode) {
                        HttpURLConnection.HTTP_CREATED -> {
                            PreyLogger.d("$uri **CREATED**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_OK -> {
                            PreyLogger.d("$uri **OK**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_CONFLICT -> {
                            PreyLogger.d("$uri **CONFLICT**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_FORBIDDEN -> {
                            PreyLogger.d("$uri **FORBIDDEN**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_MOVED_TEMP -> {
                            PreyLogger.d("$uri **MOVED_TEMP**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        422 -> {
                            PreyLogger.d("$uri **422**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_BAD_GATEWAY -> {
                            PreyLogger.d("$uri **BAD_GATEWAY**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                            PreyLogger.d("$uri **INTERNAL_ERROR**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            PreyLogger.d("$uri **NOT_FOUND**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> PreyLogger.d("$uri **gateway timeout**")
                        HttpURLConnection.HTTP_UNAVAILABLE -> PreyLogger.d("$uri**unavailable**")
                        HttpURLConnection.HTTP_NOT_ACCEPTABLE -> {
                            PreyLogger.d("$uri **NOT_ACCEPTABLE**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            PreyLogger.d("$uri **HTTP_UNAUTHORIZED**")
                            response = convertPreyHttpResponse(responseCode, connection)
                            retry = RETRIES
                        }

                        else -> PreyLogger.d("$uri **unknown response code**.")
                    }
                    connection!!.disconnect()
                    retry++
                    if (retry <= RETRIES) {
                        PreyLogger.d("Failed retry $retry/$RETRIES")
                    }
                    delay = true
                }
            } while (retry < RETRIES)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error util:" + e.message, e)
            throw e
        }

        return response
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getPostDataString(params: MutableMap<String, String?>): String {
        val result = StringBuilder()
        var first = true
        for ((key, value) in params) {
            if (first) first = false
            else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            try {
                result.append(URLEncoder.encode(value, "UTF-8"))
            } catch (e: java.lang.Exception) {
                result.append(URLEncoder.encode("", "UTF-8"))
            }
        }
        PreyLogger.d("REPORT getPostDataString:"+result.toString())
        return result.toString()
    }

    @Throws(java.lang.Exception::class)
    fun convertPreyHttpResponse(
        responseCode: Int,
        connection: HttpURLConnection
    ): PreyHttpResponse? {
        val sb = StringBuffer()
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode > 299) {
            var input: InputStream? = null
            input =
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
            if (input != null) {
                var inReader: BufferedReader? = null
                try {
                    inReader = BufferedReader(InputStreamReader(input))
                    var decodedString: String?
                    while ((inReader.readLine().also { decodedString = it }) != null) {
                        sb.append(decodedString)
                        sb.append('\r')
                    }
                } catch (e: java.lang.Exception) {
                } finally {
                    if (inReader != null) {
                        try {
                            inReader.close()
                        } catch (e: java.lang.Exception) {
                        }
                    }
                }
            }
        }
        val mapHeaderFields = connection.headerFields

        connection.disconnect()
        return PreyHttpResponse(responseCode, sb.toString(), mapHeaderFields)
    }

    fun isInternetAvailable(): Boolean {
        return true
    }


    fun postJson(uri: String, userAgent: String, jsonParam: JSONObject): PreyHttpResponse? {
        var writer: BufferedWriter? = null
        var os: OutputStream? = null
        var response: PreyHttpResponse? = null
        var conn: HttpURLConnection? = null
        try {
            val url = URL(uri)
            conn = url.openConnection() as HttpURLConnection
            conn!!.requestMethod = "POST"
            conn!!.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn!!.addRequestProperty("User-Agent", userAgent)
            os = conn!!.outputStream
            writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(jsonParam.toString())
            writer.flush()
            conn!!.connect()
            response = PreyHttpResponse(conn!!)
        } catch (e: java.lang.Exception) {
            PreyLogger.d("error postJson:" + e.message)
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: java.lang.Exception) {
                }
            }
            if (os != null) {
                try {
                    os.close()
                } catch (e: java.lang.Exception) {
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect()
                } catch (e: java.lang.Exception) {
                }
            }
        }
        return response
    }

    fun connectionJsonAuthorization(
        context: Context,
        uri: String,
        method: String?,
        jsonParam: JSONObject?
    ): PreyHttpResponse? {
        val credentials = getCredentials(PreyConfig.getInstance(context).getApiKey()!!, "X")
        return connectionJson(
            context,
            uri,
            method,
            jsonParam,
            "Basic " + credentials
        )
    }

    /**
     * Sends a JSON request to the specified URI and returns the response.
     *
     * @param config The PreyConfig object containing configuration settings.
     * @param uri The URI to send the request to.
     * @param method The HTTP method to use (e.g. "POST", "GET", etc.).
     * @param jsonParam The JSON data to send with the request.
     * @param authorization The authorization token to include with the request.
     * @return The PreyHttpResponse object containing the response from the server.
     */
    fun connectionJson(
        ctx: Context,
        uri: String,
        method: String?,
        jsonParam: JSONObject?,
        authorization: String?
    ): PreyHttpResponse? {
        // Initialize variables to track the response and retry count
        var response: PreyHttpResponse? = null
        var retryCount = 0
        var shouldDelay = false

        // Loop until we've reached the maximum number of retries
        while (retryCount < RETRIES) {
            try {
                // If we've previously failed, wait for a short period of time before retrying
                if (shouldDelay) {
                    Thread.sleep((ARRAY_RETRY_DELAY_MS[retryCount] * 1000).toLong())
                }

                // Create a URL object from the URI
                val url = URL(uri)

                // Open a connection to the URL, using HTTPS if the URI starts with "https"
                val connection =
                    if (uri.startsWith("https")) url.openConnection() as HttpsURLConnection else (url.openConnection() as HttpURLConnection)!!

                // Set up the connection properties
                connection.doOutput = true // We're sending data with the request
                connection.requestMethod = method // Set the HTTP method
                connection.useCaches = USE_CACHES // Use caching if enabled
                connection.connectTimeout = CONNECT_TIMEOUT // Set the connection timeout
                connection.readTimeout = READ_TIMEOUT // Set the read timeout

                // Set the Content-Type header to application/json
                connection.setRequestProperty("Content-Type", "application/json")

                // If an authorization token is provided, add it to the request headers
                if (authorization != null) {
                    connection.setRequestProperty("Authorization", authorization)
                }

                // Add the User-Agent and Origin headers
                connection.setRequestProperty("User-Agent", getUserAgent(ctx))
                connection.setRequestProperty("Origin", "android:com.prey")

                // Connect to the server
                connection.connect()

                // If we have JSON data to send, write it to the output stream
                if (jsonParam != null) {
                    val writer = OutputStreamWriter(connection.outputStream)
                    writer.write(jsonParam.toString())
                    writer.close()
                }

                // Get the response code from the server
                val responseCode = connection.responseCode

                // Handle the response code
                when (responseCode) {
                    HttpURLConnection.HTTP_CREATED -> {
                        // If the response was successful, convert it to a PreyHttpResponse object and exit the loop
                        response = convertPreyHttpResponse(responseCode, connection)
                        retryCount = RETRIES // exit loop
                    }

                    HttpURLConnection.HTTP_OK -> {
                        // If the response was successful, convert it to a PreyHttpResponse object and exit the loop
                        response = convertPreyHttpResponse(responseCode, connection)
                        retryCount = RETRIES // exit loop
                    }

                    else -> {
                        // If the response was not successful, increment the retry count and delay before retrying
                        retryCount++
                        shouldDelay = true
                    }
                }

                // Disconnect from the server
                connection.disconnect()
            } catch (e: java.lang.Exception) {
                // Log any errors that occur during the request
                PreyLogger.e(String.format("Error connecting to url:%s error:", uri, e.message), e)
            }
        }

        // Return the response from the server
        return response
    }


    fun uploadFile(ctx: Context, page: String, file: File, total2: Long): Int {
        var total=total2
        var responseCode = 0
        var connection: HttpURLConnection? = null
        var output: OutputStream? = null
        var input: InputStream? = null
        var fileInput: FileInputStream? = null
        PreyLogger.d(((("page:$page").toString() + " upload:" + file.name).toString() + " length:" + file.length()).toString() + " total:" + total)
        try {
            val url = URL(page)
            connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection!!.requestMethod = "POST"

            connection.addRequestProperty("Origin", "android:com.prey")
            connection.addRequestProperty("Content-Type", "application/octet-stream")
            connection.addRequestProperty("User-Agent", getUserAgent(ctx))

            if (total > 0) {
                connection.addRequestProperty("X-Prey-Upload-Resumable", "" + total)
                connection.setRequestProperty("Content-Length", "" + (file.length() - total))
                PreyLogger.d("Content-Length:" + (file.length() - total))
            } else {
                connection.setRequestProperty("Content-Length", "" + file.length())
            }
            output = connection.outputStream
            fileInput = FileInputStream(file)
            input = BufferedInputStream(fileInput)
            val maxByte = 4096
            val buffer = ByteArray(maxByte)
            var length: Int


            val dif = total - maxByte

            var read: Long = 0
            if (total > 0) {
                val maxByte2 = 4096
                var buffer2 = ByteArray(maxByte2)

                do {
                    length = 0

                    if (total < maxByte2) {
                        buffer2 = ByteArray(total as Int)
                    }
                    length = input.read(buffer2)
                    read = read + length
                    total = total - length

                    if (total <= 0) break
                    PreyLogger.d(("uploadFile total:$total").toString() + " length:" + length + " read:" + read)
                } while (total > 0)
            }
            PreyLogger.d("uploadFile read:$read")


            while ((input.read(buffer).also { length = it }) > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            val responseMessage = connection.responseMessage
            responseCode = connection.responseCode
            PreyLogger.d("uploadFile responseCode:$responseCode responseMessage:$responseMessage")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error upload:" + e.message, e)
            responseCode = 0
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
            }
            try {
                fileInput?.close()
            } catch (e: IOException) {
            }
            try {
                output?.close()
            } catch (e: IOException) {
            }
            connection?.disconnect()
        }
        return responseCode
    }


    companion object {
        private var INSTANCE: UtilConnection? = null
        fun getInstance(): UtilConnection {
            if (INSTANCE == null) {
                INSTANCE = UtilConnection()
            }
            return INSTANCE!!
        }

        var RETRIES: Int = 4


        var ARRAY_RETRY_DELAY_MS: IntArray = intArrayOf(1, 2, 3, 4)


        val REQUEST_METHOD_PUT: String = "PUT"

        val REQUEST_METHOD_POST: String = "POST"

        val REQUEST_METHOD_GET: String = "GET"

        val REQUEST_METHOD_DELETE: String = "DELETE"

        val USE_CACHES: Boolean = false

        val CONNECT_TIMEOUT: Int = 30000

        val READ_TIMEOUT: Int = 30000
    }
}