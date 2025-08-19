/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net

import android.content.Context

import com.prey.PreyLogger
import com.prey.net.http.EntityFile

import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

/**
 * A REST HTTP client for making requests to a server.
 *
 * This class provides methods for sending POST and GET requests to a server.
 * It uses the UtilConnection class to establish the connection and send the requests.
 *
 * @param context The application context.
 */
class PreyRestHttpClient(private val context: Context) {

    /**
     * Sends a POST request to the server.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun post(url: String, params: MutableMap<String, String?>): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPost(
            context,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a POST request to the server with authentication.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(Exception::class)
    fun postAutentication(url: String, params: MutableMap<String, String?>): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorization(
            context,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a POST request to the server with authentication and status.
     *
     * @param url The URL of the server.
     * @param status The status to be sent with the request.
     * @param params The parameters to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun postStatusAutentication(
        url: String,
        status: String?,
        params: MutableMap<String, String?>
    ): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? =
            UtilConnection.getInstance().connectionPostAuthorizationStatus(
                context,
                url,
                params,
                CONTENT_TYPE_URL_ENCODED,
                status
            )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a POST request to the server with authentication and entity files.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @param entityFiles The entity files to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun postAutentication(
        url: String,
        params: MutableMap<String, String?>,
        entityFiles: List<EntityFile>?
    ): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        var contentType = CONTENT_TYPE_URL_ENCODED
        if (entityFiles != null && entityFiles.size > 0) {
            contentType = ""
        }
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorization(
            context,
            url,
            params,
            contentType,
            entityFiles
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a GET request to the server.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun get(url: String, params: MutableMap<String, String?>?): PreyHttpResponse? {
        PreyLogger.d("get url: $url")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGet(
            context,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a GET request to the server with authentication.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @param user The username for authentication.
     * @param pass The password for authentication.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun get(
        url: String,
        params: MutableMap<String, String?>,
        user: String,
        pass: String
    ): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGetAuthorization(
            context,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED,
            user,
            pass
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a GET request to the server with authentication and custom content type.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @param user The username for authentication.
     * @param pass The password for authentication.
     * @param content The custom content type.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun get(
        url: String,
        params: MutableMap<String, String?>,
        user: String,
        pass: String,
        content: String
    ): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGetAuthorization(
            context,
            url,
            params,
            content,
            user,
            pass
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a GET request to the server with authentication.
     *
     * @param url The URL of the server.
     * @param params The parameters to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun getAutentication(url: String, params: MutableMap<String, String?>?): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGetAuthorization(
            context,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a POST request to the server with authentication and entity files.
     *
     * @param context The application context.
     * @param uri The URL of the server.
     * @param params The parameters to be sent with the request.
     * @param entityFiles The entity files to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun sendHelp(
        context: Context,
        uri: String,
        params: MutableMap<String, String?>,
        entityFiles: List<EntityFile>
    ): PreyHttpResponse? {
        val correlationId: String? = null
        val status: String? = null
        val authorization: String = UtilConnection.getInstance().getAuthorization(context)
        val requestMethod: String = UtilConnection.REQUEST_METHOD_POST
        val contentType = CONTENT_TYPE_URL_ENCODED
        return UtilConnection.getInstance().connection(
            context,
            uri,
            params,
            requestMethod,
            contentType,
            authorization,
            status,
            entityFiles,
            correlationId
        )
    }

    /**
     * Sends a POST request to the server with authentication and correlation ID.
     *
     * @param context The application context.
     * @param url The URL of the server.
     * @param status The status to be sent with the request.
     * @param correlation The correlation ID to be sent with the request.
     * @param params The parameters to be sent with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun postAutenticationCorrelationId(
        context: Context,
        url: String,
        status: String?,
        correlation: String?,
        params: MutableMap<String, String?>
    ): PreyHttpResponse? {
        PreyLogger.d("AWARE Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? =
            UtilConnection.getInstance().connectionPostAuthorizationCorrelationId(
                context,
                url,
                params,
                CONTENT_TYPE_URL_ENCODED,
                status,
                correlation
            )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a JSON request to the server with authentication.
     *
     * @param context The application context.
     * @param url The URL of the server.
     * @param method The HTTP method to use (e.g. "POST", "GET", etc.).
     * @param jsonParam The JSON data to send with the request.
     * @return The response from the server.
     */
    fun jsonMethodAutentication(
        context: Context,
        url: String,
        method: String,
        jsonParam: JSONObject?
    ): PreyHttpResponse? {
        var response: PreyHttpResponse? = null
        val connection: HttpURLConnection? = null
        try {
            PreyLogger.d("AWARE Sending using ${method} - URI:${url} - parameters:${jsonParam?.toString()}")

            response = UtilConnection.getInstance().connectionJsonAuthorization(
                context,
                url,
                method,
                jsonParam
            )
            if (response != null) {
                PreyLogger.d("AWARE Response from server:${response.toString()}")
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("AWARE jsonMethodAutentication:${method} error:${e.message}", e)
        } finally {
            connection?.disconnect()
        }
        return response
    }

    /**
     * Uploads a file to the server.
     *
     * @param context The application context.
     * @param url The URL of the server.
     * @param file The file to upload.
     * @param total The total size of the file.
     * @return The result of the upload operation.
     */
    fun uploadFile(context: Context, url: String, file: File, total: Long): Int {
        return UtilConnection.getInstance().uploadFile(context, url, file, total)
    }

    /**
     * Sends a DELETE request to the server with authentication.
     *
     * @param context The application context.
     * @param url The URL of the server.
     * @param params The parameters to send with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun delete(
        context: Context,
        url: String,
        params: MutableMap<String, String?>?
    ): PreyHttpResponse? {
        val response: PreyHttpResponse? =
            UtilConnection.getInstance().connectionDeleteAuthorization(
                context,
                url,
                params,
                CONTENT_TYPE_URL_ENCODED
            )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Sends a POST request to the server with authentication and a timeout.
     *
     * @param context The application context.
     * @param url The URL of the server.
     * @param params The parameters to send with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun postAutenticationTimeout(
        context: Context,
        url: String,
        params: MutableMap<String, String?>
    ): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorization(
            context,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: ${(response?.toString() ?: "")}")
        return response
    }

    /**
     * Gets a valid token from the server.
     *
     * @param context The application context.
     * @param uri The URL of the server.
     * @param json The JSON data to send with the request.
     * @return The response from the server.
     * @throws Exception If an error occurs while sending the request.
     */
    @Throws(java.lang.Exception::class)
    fun getValidToken(context: Context, uri: String, json: JSONObject): PreyHttpResponse? {
        return UtilConnection.getInstance().connectionJson(
            context,
            uri,
            UtilConnection.REQUEST_METHOD_POST,
            json
        )
    }

    companion object {
        const val CONTENT_TYPE_URL_ENCODED: String = "application/x-www-form-urlencoded"
        private var instance: PreyRestHttpClient? = null
        fun getInstance(context: Context): PreyRestHttpClient {
            return instance ?: PreyRestHttpClient(context).also { instance = it }
        }
    }

}