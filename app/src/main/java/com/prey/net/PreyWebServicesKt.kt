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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
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
        val preyConfig = PreyConfig.getPreyConfig(context)
        val authorization = UtilConnection.getAuthorization(preyConfig)
        val userAgent = UtilConnection.getUserAgent(preyConfig)
        val request = Request.Builder().url(url).addHeader("Authorization", authorization)
            .addHeader("User-Agent", userAgent).get().build()
        try {
            client.newCall(request).execute().use { response ->
                PreyLogger.d("getActionsJson code:${response.code}")
                val result = response.isSuccessful
                if (result) {
                    val body = response.body?.string()
                    PreyLogger.d("getActionsJson body:${body}")
                    return@withContext body
                } else {
                    return@withContext null
                }
            }
        } catch (e: IOException) {
            PreyLogger.e("sendNotifyActions error:${e.message}", e)
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
        PreyLogger.d("sendNotifyActions jsonData:${jsonData}")
        val contentType = "application/json; charset=utf-8".toMediaType()
        val body = jsonData.toString().toRequestBody(contentType)
        val url = PreyWebServices.getInstance().getResponseUrlJson(context)
        PreyLogger.d("sendNotifyActions url $url jsonData: $jsonData")
        val preyConfig = PreyConfig.getPreyConfig(context)
        val authorization = UtilConnection.getAuthorization(preyConfig)
        val userAgent = UtilConnection.getUserAgent(preyConfig)
        val request = Request.Builder().url(url).addHeader("Authorization", authorization)
            .addHeader("User-Agent", userAgent).post(body)
        if (correlationId != null) {
            request.addHeader("X-Prey-Correlation-ID", correlationId)
        }
        if (status != null) {
            request.addHeader("X-Prey-Status", status)
        }
        try {
            client.newCall(request.build()).execute().use { response ->
                PreyLogger.d("sendNotifyActions code:${response.code}")
                val result = response.isSuccessful
                if (result) {
                    PreyLogger.d("sendNotifyActions body:${response.body?.string()}")
                    PreyConfig.getPreyConfig(context).addActions(jsonData)
                }
                return@withContext result
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
     * Sends the device's location to the Prey server.
     *
     * This function packages the provided [Location] object into a JSON payload and POSTs it
     * to the Prey API. If the `isAware` flag is true, it first checks the distance from the
     * last reported location. If the device has moved less than 250 meters, the new location
     * is not sent, to avoid redundant updates. On a successful transmission, the current
     * location is saved as the last known location.
     *
     * This operation is performed on a background thread.
     *
     * @param context The application context.
     * @param location The location data to send.
     * @param isAware A boolean flag indicating if the location report is for Aware mode.
     *                This triggers a check to prevent sending updates if the location hasn't
     *                changed significantly.
     * @return `true` if the location was sent successfully, `false` otherwise (including
     *         if the location was not sent due to minimal distance change in Aware mode,
     *         or if a network error occurred).
     */
    suspend fun doSendLocation(
        context: Context, location: Location, isAware: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        PreyLogger.d("doSendLocation:")
        if (isAware) {
            val previous = AwareStore.load(context)
            var distanceInMeters = -1f
            if (previous != null) {
                distanceInMeters = location.distanceTo(previous.location)
            }
            PreyLogger.d("distanceInMeters:${distanceInMeters}")
            if (distanceInMeters in 0.0..250.0) {
                PreyLogger.d("distanceInMeters return")
                return@withContext false
            }
        }
        PreyLogger.d("doSendLocation doSend")
        val accuracy = (location.accuracy * 100.0).roundToInt() / 100.0
        // Create JSON object for location data
        val json = JSONObject()
        json.put(LocationUtil.LAT, location.latitude)
        json.put(LocationUtil.LNG, location.longitude)
        json.put(LocationUtil.ACC, accuracy)
        json.put(LocationUtil.METHOD, "native")
        val jsonData = JSONObject()
        // Put location data into location wrapper
        jsonData.put("location", json)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonData.toString().toRequestBody(mediaType)
        val preyConfig = PreyConfig.getPreyConfig(context)
        val authorization = UtilConnection.getAuthorization(preyConfig)
        val userAgent = UtilConnection.getUserAgent(preyConfig)
        val url = if (isAware) {
            PreyWebServices.getInstance().getDataUrlJson(context)
            //PreyWebServices.getInstance().getLocationUrlJson(context)
        } else {
            PreyWebServices.getInstance().getDataUrlJson(context)
        }
        PreyLogger.d("doSendLocation url $url jsonData: $jsonData")
        val request = Request.Builder().url(url).addHeader("Authorization", authorization)
            .addHeader("User-Agent", userAgent).post(body).build()
        try {
            client.newCall(request).execute().use { response ->
                val result = response.isSuccessful
                PreyLogger.d("doSendLocation result:${result}")
                if (result) {
                    PreyLogger.d("doSendLocation body:${response.body?.string()}")
                    AwareStore.save(context, location)
                }
                return@withContext result
            }
        } catch (e: IOException) {
            PreyLogger.e("doSendLocation error:${e.message}", e)
            return@withContext false
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
        val jsonString = rootJson.toString()
        PreyLogger.d("sendPreyHttpEvent jsonString: $jsonString")
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonString.toRequestBody(mediaType)
        //Request configuration
        val preyConfig = PreyConfig.getPreyConfig(context)
        val url = PreyWebServices.getInstance().getEventsUrlJson(context)
        PreyLogger.d("sendPreyHttpEvent url:${url}")
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", UtilConnection.getAuthorization(preyConfig))
            .addHeader("X-Prey-Status", extraData.toString())
            .addHeader("User-Agent", UtilConnection.getUserAgent(preyConfig))
            .post(body)
            .build()
        //Execution and response management
        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    PreyLogger.d("sendPreyHttpEvent successful Code:${response.code} responseBody:${responseBody}")
                    responseBody
                } else {
                    PreyLogger.d("sendPreyHttpEvent error Code:${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Unexpected error in sendPreyHttpEvent", e)
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
        PreyLogger.d("DailyLocationSender send")
        val accuracy = (location.accuracy * 100.0).roundToInt() / 100.0
        // Create JSON object for location data
        val json = JSONObject()
        json.put(LocationUtil.LAT, location.latitude)
        json.put(LocationUtil.LNG, location.longitude)
        json.put(LocationUtil.ACC, accuracy)
        json.put(LocationUtil.METHOD, "native")
        json.put(LocationUtil.FORCE, true)
        val jsonData = JSONObject()
        // Put location data into location wrapper
        jsonData.put("location", json)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonData.toString().toRequestBody(mediaType)
        val preyConfig = PreyConfig.getPreyConfig(context)
        val authorization = UtilConnection.getAuthorization(preyConfig)
        val userAgent = UtilConnection.getUserAgent(preyConfig)
        val url = PreyWebServices.getInstance().getDataUrlJson(context)
        //var url = PreyWebServices.getInstance().getLocationUrlJson(context)
        PreyLogger.d("url:${url}")
        val request =
            Request.Builder().url(url).post(body).addHeader("Authorization", authorization)
                .addHeader("User-Agent", userAgent).build()
        try {
            client.newCall(request).execute().use { response ->
                PreyLogger.d("LocationSender send code:${response.code}")
                val result = response.isSuccessful
                PreyLogger.d("LocationSender send result:${result}")
                if (result) {
                    PreyLogger.d("LocationSender send body:${response.body?.string()}")
                }
                return result
            }
        } catch (e: IOException) {
            PreyLogger.e("doSendLocation send error:${e.message}", e)
            return false
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
    fun getIPAddress(): String? {
        val request = Request.Builder()
            .url("https://ifconfig.me/ip")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string()?.trim()
            }
        } catch (e: Exception) {
            PreyLogger.e("Connection error:${e.message}",e)
            null
        }
    }

}