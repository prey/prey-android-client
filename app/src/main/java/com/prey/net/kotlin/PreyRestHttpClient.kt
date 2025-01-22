/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.kotlin

import android.content.Context
import com.prey.kotlin.PreyLogger
import com.prey.net.http.kotlin.EntityFile
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

class PreyRestHttpClient {

    var mContext: Context

    constructor(context: Context) {
        mContext = context
    }

    val CONTENT_TYPE_URL_ENCODED: String = "application/x-www-form-urlencoded"


    @Throws(java.lang.Exception::class)
    fun post(url: String, params: MutableMap<String, String?>): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPost(
            mContext,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }
    @Throws(Exception::class)
    fun postAutentication(url: String, params: MutableMap<String, String?>): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorization(
            mContext,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: " + (response!!.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun postStatusAutentication(
        url: String,
        status: String?,
        params: MutableMap<String, String?>
    ): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorizationStatus(
            mContext,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED,
            status
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun postAutentication( url: String, params: MutableMap<String, String?>, entityFiles: List<EntityFile>?): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        var contentType = CONTENT_TYPE_URL_ENCODED
        if (entityFiles != null && entityFiles.size > 0) {
            contentType = ""
        }
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorization(
            mContext,
            url,
            params,
            contentType,
            entityFiles
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun get(url: String, params: MutableMap<String, String?>?): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGet(
            mContext,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun get(
        url: String,
        params: MutableMap<String, String?>,
        user: String,
        pass: String
    ): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGetAuthorization(
            mContext,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED,
            user,
            pass
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun get(
        url: String,
        params: MutableMap<String, String?>,
        user: String,
        pass: String,
        content: String
    ): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGetAuthorization(
            mContext,
            url,
            params,
            content,
            user,
            pass
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun getAutentication(url: String, params: MutableMap<String, String?>?): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionGetAuthorization(
            mContext,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    /**
     * Method to send the help
     *
     * @param ctx
     * @param uri
     * @param params
     * @param entityFiles
     * @return  help result
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun sendHelp(
        ctx: Context,
        uri: String,
        params: MutableMap<String, String?>,
        entityFiles: List<EntityFile>
    ): PreyHttpResponse? {
        val correlationId: String? = null
        val status: String? = null
        val authorization: String = UtilConnection.getInstance().getAuthorization(ctx)
        val requestMethod: String = UtilConnection.REQUEST_METHOD_POST
        val contentType = CONTENT_TYPE_URL_ENCODED
        return UtilConnection.getInstance().connection(
            ctx,
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

    @Throws(java.lang.Exception::class)
    fun postAutenticationCorrelationId(
        ctx: Context,
        url: String,
        status: String?,
        correlation: String?,
        params: MutableMap<String, String?>
    ): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        var response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorizationCorrelationId(
            ctx,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED,
            status,
            correlation
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    fun jsonMethodAutentication(
        ctx: Context,
        url: String,
        method: String,
        jsonParam: JSONObject?
    ): PreyHttpResponse? {
        var response: PreyHttpResponse? = null
        val connection: HttpURLConnection? = null
        try {
            PreyLogger.d(
                String.format(
                    "Sending using %s - URI:%s - parameters:%s", method, url, (jsonParam?.toString()
                        ?: "")
                )
            )
            response = UtilConnection.getInstance().connectionJsonAuthorization(
                 ctx,
                url,
                method,
                jsonParam
            )
            if (response != null) {
                PreyLogger.d(String.format("Response from server:%s", response.toString()))
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e(String.format("jsonMethodAutentication:%s error:%s", method, e.message), e)
        } finally {
            connection?.disconnect()
        }
        return response
    }

    fun uploadFile(ctx: Context, url: String, file: File, total: Long): Int {
        return UtilConnection.getInstance().uploadFile(ctx, url, file, total)
    }

    @Throws(java.lang.Exception::class)
    fun delete(ctx: Context,url: String, params: MutableMap<String, String?>?): PreyHttpResponse? {
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionDeleteAuthorization(
            ctx,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }

    @Throws(java.lang.Exception::class)
    fun postAutenticationTimeout(ctx: Context,url: String, params: MutableMap<String, String?>): PreyHttpResponse? {
        PreyLogger.d("Sending using 'POST' - URI: $url - parameters: $params")
        val response: PreyHttpResponse? = UtilConnection.getInstance().connectionPostAuthorization(
            ctx,
            url,
            params,
            CONTENT_TYPE_URL_ENCODED
        )
        PreyLogger.d("Response from server: " + (response?.toString() ?: ""))
        return response
    }
    companion object {
        private var INSTANCE: PreyRestHttpClient? = null
        fun getInstance(context: Context): PreyRestHttpClient {
            if (PreyRestHttpClient.INSTANCE == null) {
                PreyRestHttpClient.INSTANCE = PreyRestHttpClient(context)
            }
            return PreyRestHttpClient.INSTANCE!!
        }
    }
}