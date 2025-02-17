/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net

import android.content.Context
import android.os.Build
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

/**
 * Utility class for establishing connections and making HTTP requests.
 */
class UtilConnection {

    /**
     * Generates the Basic Authentication credentials for the given user and password.
     *
     * @param user The username.
     * @param password The password.
     * @return The Base64 encoded credentials.
     */
    private fun getCredentials(user: String, password: String): String {
        return Base64.encodeToString(("$user:$password").toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Generates the User-Agent string for the given context.
     *
     * @param context The application context.
     * @return The User-Agent string.
     */
    private fun getUserAgent(context: Context): String {
        val version = PreyConfig.getInstance(context).getPreyVersion()
        val build = PreyUtils.getBuildVersionRelease()
        return "Prey/${version} (Android ${build})"
    }

    /**
     * Generates the Basic Authentication header for the given context.
     *
     * @param context The application context.
     * @return The Basic Authentication header.
     */
    fun getAuthorization(context: Context): String {
        val credentials = getCredentials(PreyConfig.getInstance(context).getApiKey()!!, "X")
        return "Basic $credentials"
    }

    /**
     * Generates the Basic Authentication header for the given user and password.
     *
     * @param user The username.
     * @param pass The password.
     * @return The Basic Authentication header.
     */
    private fun getAuthorization(user: String, pass: String): String {
        return "Basic ${getCredentials(user, pass)}"
    }

    /**
     * Establishes a connection and sends a PUT request with the given parameters.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    public fun connectionPut(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
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

    /**
     * Establishes a connection and sends a PUT request with the given parameters, using Basic Authentication.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionPutAuthorization(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
            uri,
            params,
            REQUEST_METHOD_PUT,
            contentType,
            getAuthorization(context),
            null,
            null,
            null
        )
    }

    /**
     * Establishes a connection and sends a GET request with the given parameters.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionGet(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
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

    /**
     * Establishes a connection and sends a GET request with the given parameters and authorization.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionGetAuthorization(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
            uri,
            params,
            REQUEST_METHOD_GET,
            contentType,
            getAuthorization(context),
            null,
            null,
            null
        )
    }

    /**
     * Establishes a connection and sends a GET request with the given parameters and authorization.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @param user The username for authorization.
     * @param pass The password for authorization.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionGetAuthorization(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String,
        user: String,
        pass: String
    ): PreyHttpResponse? {
        return connection(
            context,
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

    /**
     * Establishes a connection and sends a DELETE request with the given parameters.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionDelete(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
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

    /**
     * Establishes a connection and sends a DELETE request with the given parameters and authorization.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(java.lang.Exception::class)
    fun connectionDeleteAuthorization(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String?
    ): PreyHttpResponse? {
        return connection(
            context,
            uri, params, com.prey.net.UtilConnection.REQUEST_METHOD_DELETE,
            contentType!!, getAuthorization(context), null, null, null
        )
    }

    /**
     * Establishes a connection and sends a POST request with the given parameters.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionPost(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
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

    /**
     * Establishes a connection and sends a POST request with the given parameters and authorization.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionPostAuthorization(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String
    ): PreyHttpResponse? {
        return connection(
            context,
            uri,
            params,
            REQUEST_METHOD_POST,
            contentType,
            getAuthorization(context),
            null,
            null,
            null
        )
    }

    /**
     * Establishes a connection and sends a POST request with the given parameters, authorization, and entity files.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @param entityFiles The entity files to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(java.lang.Exception::class)
    fun connectionPostAuthorization(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String?,
        entityFiles: List<EntityFile>?
    ): PreyHttpResponse? {
        return connection(
            context,
            uri, params, REQUEST_METHOD_POST,
            contentType!!, getAuthorization(context), null, entityFiles, null
        )
    }

    /**
     * Establishes a connection and sends a POST request with the given parameters, authorization, status, and content type.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @param status The status to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(java.lang.Exception::class)
    fun connectionPostAuthorizationStatus(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        contentType: String?,
        status: String?
    ): PreyHttpResponse? {
        return connection(
            context,
            uri, params, com.prey.net.UtilConnection.REQUEST_METHOD_POST,
            contentType!!, getAuthorization(context), status, null, null
        )
    }

    /**
     * Establishes a connection and sends a POST request with the given parameters,
     * authorization, status, and correlation ID.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param contentType The content type of the request.
     * @param status The status to be sent with the request.
     * @param correlationId The correlation ID to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(Exception::class)
    fun connectionPostAuthorizationCorrelationId(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        contentType: String,
        status: String?,
        correlationId: String?
    ): PreyHttpResponse? {
        return connection(
            context,
            uri,
            params,
            REQUEST_METHOD_POST,
            contentType,
            getAuthorization(context),
            status,
            null,
            correlationId
        )
    }

    /**
     * Establishes a connection and sends a request with the given parameters.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param requestMethod The request method (e.g. GET, POST, PUT, DELETE).
     * @param contentType The content type of the request.
     * @param authorization The authorization token for the request.
     * @param status The status to be sent with the request.
     * @param entityFiles The entity files to be sent with the request.
     * @param correlationId The correlation ID to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
    @Throws(java.lang.Exception::class)
    fun connection(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>?,
        requestMethod: String,
        contentType: String,
        authorization: String?,
        status: String?,
        entityFiles: List<EntityFile>?,
        correlationId: String?
    ): PreyHttpResponse? {
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        var response: PreyHttpResponse? = null
        val url = URL(uri)
        var connection: HttpURLConnection? = null
        var retry = 0
        var delay = false
        if (params != null) {
            val ite = params.keys.iterator()
            while (ite.hasNext()) {
                val key = ite.next()
                PreyLogger.d("[${key}]:${params[key]}")
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
                val deviceId = PreyConfig.getInstance(context).getDeviceId()
                if (deviceId != null) {
                    connection.addRequestProperty("X-Prey-Device-ID", deviceId)
                    PreyLogger.d("X-Prey-Device-ID:$deviceId")
                    connection.addRequestProperty("X-Prey-State", status)
                    PreyLogger.d("X-Prey-State:$status")
                }
                val userAgent = getUserAgent(context)
                connection.addRequestProperty("User-Agent", userAgent)
                PreyLogger.d("User-Agent:${userAgent}")
                connection.addRequestProperty("Origin", "android:com.prey")
                if (params != null) {
                    PreyLogger.d("REPORT params__________:${params.size}")
                }
                PreyLogger.d("REPORT entityFiles is null:${(entityFiles == null)}")
                if (entityFiles == null && (params != null && params.size > 0)) {
                    val os: OutputStream = connection.outputStream
                    val dos = DataOutputStream(os)
                    dos.writeBytes(getPostDataString(params))
                }
                if (entityFiles != null && entityFiles.size > 0) {
                    for ((key, value1) in params!!.entries) {
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
                    while (entityFiles != null && i < entityFiles.size) {
                        val entityFile = entityFiles[i]
                        var isLast = false
                        if ((i + 1) == entityFiles.size) {
                            isLast = true
                        }
                        val outputStream = multiple.addPart(
                            entityFile.getName()!!,
                            entityFile.getFileName()!!,
                            entityFile.getFileInputStream(),
                            entityFile.getFileType()!!,
                            isLast
                        )
                        listOutputStream.add(outputStream!!)
                        i++
                    }
                    connection.setRequestProperty(
                        "Content-Length",
                        "${multiple.contentLength()}"
                    )
                    connection.setRequestProperty("Content-Type", multiple.contentType())
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

            } while (retry < RETRIES)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("error util:${e.message}", e)
            throw e
        }
        return response
    }

    /**
     * Establishes a connection and sends a request with the given parameters.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param params The request parameters.
     * @param requestMethod The request method (e.g. GET, POST, PUT, DELETE).
     * @param contentType The content type of the request.
     * @param authorization The authorization token for the request.
     * @param status The status to be sent with the request.
     * @param entityFiles The entity files to be sent with the request.
     * @param correlationId The correlation ID to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs during the connection or request.
     */
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
        PreyLogger.d("REPORT getPostDataString:${result.toString()}")
        return result.toString()
    }

    /**
     * Converts an HTTP response to a PreyHttpResponse object.
     *
     * @param responseCode the HTTP response code
     * @param connection the HttpURLConnection object
     * @return the PreyHttpResponse object, or null if an error occurs
     * @throws Exception if an error occurs during conversion
     */
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

    /**
     * Sends a JSON POST request to the specified URI and returns the response.
     *
     * @param uri The URI to send the request to.
     * @param userAgent The User-Agent header to include with the request.
     * @param jsonParam The JSON data to send with the request.
     * @return The PreyHttpResponse object containing the response from the server.
     */
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
            PreyLogger.d("error postJson:${e.message}")
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

    /**
     * Sends a JSON request to the specified URI with authorization and returns the response.
     *
     * @param context The application context.
     * @param uri The URI to send the request to.
     * @param method The HTTP method to use (e.g. "POST", "GET", etc.).
     * @param jsonParam The JSON data to send with the request.
     * @return The PreyHttpResponse object containing the response from the server.
     */
    fun connectionJsonAuthorization(
        context: Context,
        uri: String,
        method: String,
        jsonParam: JSONObject?
    ): PreyHttpResponse? {
        val credentials = getCredentials(PreyConfig.getInstance(context).getApiKey()!!, "X")
        return connectionJson(
            context,
            uri,
            method,
            jsonParam,
            "Basic $credentials"
        )
    }

    /**
     * Sends a JSON request to the specified URI and returns the response.
     *
     * @param context The application context.
     * @param uri The URI to send the request to.
     * @param method The HTTP method to use (e.g. "POST", "GET", etc.).
     * @param jsonParam The JSON data to send with the request.
     * @return The PreyHttpResponse object containing the response from the server.
     */
    fun connectionJson(
        context: Context,
        uri: String,
        method: String,
        jsonParam: JSONObject?
    ): PreyHttpResponse? {
        return connectionJson(context, uri, REQUEST_METHOD_POST, jsonParam, null)
    }

    /**
     * Establishes a connection and sends a JSON request to a server.
     *
     * @param context The application context.
     * @param uri The request URI.
     * @param method The HTTP method (e.g. GET, POST, PUT, DELETE).
     * @param jsonParam The JSON data to send with the request.
     * @param authorization The authorization token for the request.
     * @return The response from the server.
     */
    fun connectionJson(
        context: Context,
        uri: String,
        method: String,
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
                connection.setRequestProperty("User-Agent", getUserAgent(context))
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
                PreyLogger.e("Error connecting to url:${uri} error:${e.message}", e)
            }
        }
        // Return the response from the server
        return response
    }

    /**
     * Uploads a file to a specified URL.
     *
     * @param context The application context.
     * @param page The URL to upload the file to.
     * @param file The file to upload.
     * @param total The total size of the file.
     * @return The HTTP response code.
     */
    fun uploadFile(context: Context, page: String, file: File, total2: Long): Int {
        var total = total2
        var responseCode = 0
        var connection: HttpURLConnection? = null
        var output: OutputStream? = null
        var input: InputStream? = null
        var fileInput: FileInputStream? = null
        PreyLogger.d("page:${page} upload:${file.name} length:${file.length()} total:${total}")
        try {
            val url = URL(page)
            connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection!!.requestMethod = "POST"
            connection.addRequestProperty("Origin", "android:com.prey")
            connection.addRequestProperty("Content-Type", "application/octet-stream")
            connection.addRequestProperty("User-Agent", getUserAgent(context))
            if (total > 0) {
                connection.addRequestProperty("X-Prey-Upload-Resumable", "${total}")
                connection.setRequestProperty("Content-Length", "${(file.length() - total)}")
                PreyLogger.d("Content-Length:${(file.length() - total)}")
            } else {
                connection.setRequestProperty("Content-Length", "${file.length()}")
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
                    PreyLogger.d("uploadFile total:${total} length:${length} read:${read}")
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
            PreyLogger.e("error upload:${e.message}", e)
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

    /**
     * Method check if you have internet
     *
     * @return available
     */
    fun isInternetAvailable(): Boolean = true

    companion object {
        private var instance: UtilConnection? = null
        fun getInstance(): UtilConnection {
            return instance ?: UtilConnection().also { instance = it }
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