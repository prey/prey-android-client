/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net

import android.content.Context
import android.location.Location
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareStore
import com.prey.actions.location.LocationUtil
import com.prey.events.Event
import com.prey.json.UtilJson
import com.prey.net.PreyWebServicesKt.sendNotifyActions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt

/**
 * A singleton object that handles all web service communications with the Prey servers.
 *
 * This object is responsible for:
 * - Fetching actions (commands) from the Prey panel for the current device.
 * - Sending back responses/notifications for the executed actions.
 * - Sending device location data to the server, both for standard reports and for Aware mode.
 *
 * All network operations are performed asynchronously using Kotlin Coroutines on an I/O-optimized thread.
 */
object PreyWebServicesKt {

    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Internal helper to construct an OkHttp [Request.Builder] with common Prey headers.
     *
     * This method centralizes the logic for adding required authentication and user-agent
     * headers to every outgoing request.
     *
     * @param url The full destination URL for the request.
     * @param context The application context, used to retrieve the current [PreyConfig].
     * @param method The HTTP method to use (e.g., "GET", "POST", "PUT"). Defaults to "GET".
     * @param body The optional [RequestBody] for methods that send data (like POST).
     * @return A [Request.Builder] initialized with the URL, headers, and method.
     */
    private fun buildRequest(
        url: String,
        context: Context,
        method: String = "GET",
        body: RequestBody? = null
    ): Request.Builder {
        val config = PreyConfig.getPreyConfig(context)
        return Request.Builder()
            .url(url)
            .addHeader("Authorization", UtilConnection.getAuthorization(config))
            .addHeader("User-Agent", UtilConnection.getUserAgent(config))
            .method(method, body)
    }

    /**
     * Converts a [Location] object into a [JSONObject] formatted for the Prey web services.
     *
     * This extension function extracts coordinates and accuracy, rounding the accuracy to
     * two decimal places. It structures the data within a "location" root object required
     * by the server API.
     *
     * @param force If `true`, adds a force flag to the location data, typically used to
     *              override server-side throttling or movement filters.
     * @return A [JSONObject] containing the formatted location metadata.
     */
    private fun Location.toPreyJson(force: Boolean = false): JSONObject {
        val accuracy = (this.accuracy * 100.0).roundToInt() / 100.0
        val locationData = JSONObject().apply {
            put(LocationUtil.LAT, latitude)
            put(LocationUtil.LNG, longitude)
            put(LocationUtil.ACC, accuracy)
            put(LocationUtil.METHOD, "native")
            if (force) put(LocationUtil.FORCE, true)
        }
        return JSONObject().put("location", locationData)
    }

    /**
     * Fetches pending actions for the device from the Prey server.
     *
     * This function performs a GET request to the device-specific URL to retrieve a JSON
     * string containing commands (actions) to be executed on the device, such as
     * triggering an alarm, taking a picture, or locking the screen.
     *
     * The request is authenticated using the device's API key.
     *
     * This is a suspending function and should be called from a coroutine scope. It handles
     * network operations on an I/O-optimized thread.
     *
     * @param context The application context, used to access shared configurations like API keys.
     * @return A JSON string containing the list of actions if the request is successful,
     *         or `null` if the request fails, the response is not successful, or a network
     *         error occurs.
     */
    suspend fun getActionsJson(context: Context): String? = withContext(Dispatchers.IO) {
        val url = PreyWebServices.getInstance().getDeviceUrlJson(context)
        PreyLogger.d("getActionsJson url:${url}")
        val request = buildRequest(url, context).build()
        try {
            client.newCall(request).execute().use { response ->
                PreyLogger.d("getActionsJson code:${response.code}")
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    PreyLogger.d("getActionsJson body:${body}")
                    return@withContext body
                } else {
                    return@withContext null
                }
            }
        } catch (e: IOException) {
            PreyLogger.e("getActionsJson error:${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Sends a notification or response for an executed action to the Prey servers.
     *
     * This function performs an asynchronous POST request to the Prey panel, sending a JSON payload
     * with the results or status of an action that was previously received and processed by the client.
     * It can also include optional headers for correlation and status reporting.
     *
     * @param context The application context, used to access configuration and other resources.
     * @param jsonData The [JSONObject] payload to be sent as the body of the request. This typically contains
     *                 the results of an action.
     * @param correlationId An optional string used to correlate this response with the original request
     *                      that triggered the action. Sent as the `X-Prey-Correlation-ID` header.
     * @param status An optional string indicating the outcome of the action (e.g., "started", "finished").
     *               Sent as the `X-Prey-Status` header.
     * @return `true` if the request was successfully sent and received a success (2xx) HTTP status code,
     *         `false` otherwise (e.g., network error, server error).
     */
    suspend fun sendNotifyActions(
        context: Context, jsonData: JSONObject?, correlationId: String?, status: String?
    ): Boolean = withContext(Dispatchers.IO) {
        if (jsonData == null) return@withContext false
        val url = PreyWebServices.getInstance().getResponseUrlJson(context)
        val body = jsonData.toString().toRequestBody(JSON_MEDIA_TYPE)
        val requestBuilder = buildRequest(url, context, "POST", body)
        correlationId?.let { requestBuilder.addHeader("X-Prey-Correlation-ID", it) }
        status?.let { requestBuilder.addHeader("X-Prey-Status", it) }
        try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                PreyLogger.d("sendNotifyActions code:${response.code}")
                if (response.isSuccessful) {
                    PreyLogger.d("sendNotifyActions body:${response.body?.string()}")
                    PreyConfig.getPreyConfig(context).addActions(jsonData)
                }
                return@withContext response.isSuccessful
            }
        } catch (e: IOException) {
            PreyLogger.e("sendNotifyActions error:${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Constructs and sends a notification response to the Prey server for a specific command.
     *
     * This method simplifies the notification process by wrapping the parameters into a JSON
     * object and delegating the network request to [sendNotifyActions].
     *
     * @param context The application context.
     * @param command The name of the action/command being reported (e.g., "get_location").
     * @param target The target of the command.
     * @param status The current status of the action execution (e.g., "started", "finished").
     * @param reason An optional description or error message regarding the status.
     * @param messageId An optional correlation ID to link this notification to a specific request.
     * @param progress An optional string representing the execution progress.
     */
    suspend fun notify(
        context: Context,
        command: String,
        target: String,
        status: String,
        reason: String? = null,
        messageId: String? = null,
        progress: String? = null
    ) {
        withContext(Dispatchers.IO) {
            val json = UtilJson.makeJsonResponse(command, target, status, reason)
            sendNotifyActions(context, json, messageId, progress)
        }
    }

    /**
     * Reports a specific device event to the Prey server via an HTTP POST request.
     *
     * This function constructs a JSON payload containing the event's name, information, and
     * additional metadata. It also includes the metadata in the `X-Prey-Status` header.
     * The request is authenticated using the device's credentials and user agent.
     *
     * This is a suspending function that executes the network call on the [Dispatchers.IO]
     * coroutine dispatcher to ensure the calling thread is not blocked.
     *
     * @param context The application context, used to retrieve server URLs and device configuration.
     * @param event An [Event] object containing the name and basic information of the event.
     * @param extraData A [JSONObject] containing supplementary data or status information to be sent.
     * @return The response body as a [String] if the request was successful; `null` if the
     *         server returned an error or if an exception occurred during the process.
     */
    suspend fun sendPreyHttpEvent(
        context: Context,
        event: Event,
        extraData: JSONObject
    ): String? = withContext(Dispatchers.IO) {
        //JSON Payload Construction
        val rootJson = JSONObject().apply {
            put("name", event.name)
            put("info", event.info)
            put("status", extraData)
        }
        val url = PreyWebServices.getInstance().getEventsUrlJson(context)
        val body = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = buildRequest(url, context, "POST", body)
            .addHeader("X-Prey-Status", extraData.toString())
            .build()
        //Execution and response management
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    PreyLogger.d("sendPreyHttpEvent successful:${response.code} responseBody:${responseBody}")
                    responseBody
                } else {
                    PreyLogger.d("sendPreyHttpEvent code:${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("sendPreyHttpEvent Unexpected error:${e.message}", e)
            null
        }
    }

    /**
     * Sends the captured location data to the Prey web services.
     *
     * This method processes the [location] coordinates and accuracy, constructs a JSON payload,
     * and performs an authenticated POST request to the server.
     *
     * @param context The application [Context] used to retrieve configurations and URLs.
     * @param location The [Location] object containing the coordinates to be sent.
     * @return `true` if the location was successfully sent and received by the server, `false` otherwise.
     */
    fun sendDailyLocation(context: Context, location: Location): Boolean {
        PreyLogger.d("sendDailyLocation")
        val url = PreyWebServices.getInstance().getDataUrlJson(context)
        //var url = PreyWebServices.getInstance().getLocationUrlJson(context)
        val body = location.toPreyJson(true).toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = buildRequest(url, context, "POST", body).build()
        PreyLogger.d("doSendLocation url $url jsonData: $location.toPreyJson()")
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    PreyLogger.d("sendDailyLocation body:${response.body?.string()}")
                }
                return response.isSuccessful
            }
        } catch (e: IOException) {
            PreyLogger.e("sendDailyLocation error:${e.message}", e)
            return false
        }
    }

    /**
     * Sends the current location to the Prey server, with optional smart filtering for Aware mode.
     *
     * In Aware mode ([isAware] is true), this function implements a distance-based filter to
     * avoid redundant uploads; if the device has moved 250 meters or less since the last
     * reported location, the upload is skipped to conserve battery and bandwidth.
     *
     * Successful uploads result in the location being cached locally via [AwareStore] for
     * future distance comparisons.
     *
     * @param context The application context used for configuration and storage.
     * @param location The [Location] object containing the coordinates and accuracy.
     * @param isAware Whether the request is triggered by Aware mode. If true, enables distance filtering.
     * @return `true` if the location was successfully sent to the server, `false` if the
     *         upload was skipped due to proximity, or if a network error occurred.
     */
    suspend fun doSendLocation(
        context: Context, location: Location, isAware: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        PreyLogger.d("doSendLocation")
        if (isAware) {
            val previous = AwareStore.load(context)
            if (previous != null && location.distanceTo(previous.location) <= 250) {
                return@withContext false
            }
        }
        val url = if (isAware) {
            PreyWebServices.getInstance().getDataUrlJson(context)
            //PreyWebServices.getInstance().getLocationUrlJson(context)
        } else {
            PreyWebServices.getInstance().getDataUrlJson(context)
        }
        val body = location.toPreyJson().toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = buildRequest(url, context, "POST", body).build()
        PreyLogger.d("doSendLocation url $url jsonData: $location.toPreyJson()")
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    PreyLogger.d("doSendLocation body:${response.body?.string()}")
                    AwareStore.save(context, location)
                }
                return@withContext response.isSuccessful
            }
        } catch (e: IOException) {
            PreyLogger.e("doSendLocation error:${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Retrieves the name of the device as registered on the Prey server.
     *
     * This function performs an authenticated GET request to the device information URL.
     * It parses the resulting JSON response to extract the "name" field.
     *
     * This is a suspending function that executes the network operation on the
     * [Dispatchers.IO] coroutine dispatcher.
     *
     * @param context The application context used to retrieve device configurations and URLs.
     * @return The name of the device if the request is successful and the "name" field exists;
     *         `null` if the request fails, the response is empty, or an error occurs during parsing.
     */
     suspend fun getNameDevice(context: Context): String? = withContext(Dispatchers.IO){
        val url = PreyWebServices.getInstance().getInfoUrlJson(context)
        val request = buildRequest(url, context).build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                PreyLogger.d("getNameDevice code:${response.code} | success:${response.isSuccessful}")
                if (response.isSuccessful && !body.isNullOrBlank()) {
                    JSONObject(body).optString("name", null)
                } else {
                    PreyLogger.d("getNameDevice failed: ${response.message}")
                    null
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("getNameDevice error: ${e.message}", e)
            null
        }
    }

    /**
     * Retrieves the device's current public IP address.
     *
     * This function performs a synchronous GET request to an external service (ifconfig.me)
     * to determine the external-facing IP address of the network the device is connected to.
     *
     * @return The public IP address as a trimmed [String] if successful; `null` if the
     *         request fails, returns a non-success status code, or an exception occurs.
     */
    suspend fun getIPAddress(): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://ifconfig.me/ip")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string()?.trim() else null
            }
        } catch (e: Exception) {
            PreyLogger.e("Connection error:${e.message}", e)
            null
        }
    }


    suspend fun getProfile(context: Context): String? = withContext(Dispatchers.IO) {
        val url = PreyWebServices.getInstance().getProfileUrl(context)
        PreyLogger.d("getProfile url:${url}")
        val request = buildRequest(url, context).build()
        try {
            client.newCall(request).execute().use { response ->
                PreyLogger.d("getProfile code:${response.code}")
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    PreyLogger.d("getProfile body:${body}")
                    return@withContext body
                } else {
                    return@withContext null
                }
            }
        } catch (e: IOException) {
            PreyLogger.e("getProfile error:${e.message}", e)
            return@withContext null
        }
    }


}