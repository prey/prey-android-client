/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.HttpDataService
import com.prey.actions.location.LocationUtil
import com.prey.json.CommandTarget
import com.prey.json.UtilJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import kotlin.math.roundToInt

/**
 * Handles the "location_low_battery" action, which retrieves and reports the device's
 * geographical location to the Prey server when a low battery state is detected.
 *
 * This object implements the [CommandTarget] interface to process incoming commands
 * asynchronously using Kotlin Coroutines. It ensures that the necessary location
 * permissions are granted before attempting to acquire the coordinates and
 * transmits the data via [HttpDataService].
 */
object LocationLowBattery : CommandTarget, BaseAction() {

    const val TARGET = "location_low_battery"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> getCoroutine(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    fun getCoroutine(context: Context, options: JSONObject) {
        scope.launch { get(context, options) }
    }

    /**
     * Performs the "get" command to retrieve and report the device's current location specifically
     * for low battery events.
     *
     * This function checks for necessary location permissions, attempts to fetch the current
     * GPS/network location within a defined timeout, and sends the coordinates to the
     * Prey web services. It updates the action status to 'started', and then to either
     * 'stopped' (on success) or 'failed' (on permission denial, timeout, or network error).
     *
     * @param context The application context.
     */
    suspend fun get(context: Context, options: JSONObject) = withContext(Dispatchers.IO) {
        val config = PreyConfig.getPreyConfig(context)
        config.addActions(UtilJson.makeJsonResponse(CMD_GET, TARGET, STATUS_STARTED))
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            PreyLogger.d("Permission denied for location")
            config.addActions(UtilJson.makeJsonResponse(CMD_GET, TARGET, STATUS_FAILED, "not_permission"))
            return@withContext
        }
        val location: Location? = withTimeoutOrNull(com.prey.json.actions.Location.LOCATION_TIMEOUT) {
            com.prey.json.actions.Location.getLocation(context)
        }
        if (location == null) {
            config.addActions(UtilJson.makeJsonResponse(CMD_GET, TARGET, STATUS_FAILED, "not_data"))
            return@withContext
        }
        val data = convertData(location)
        data!!.key = "location_low_battery"
        val dataToBeSent = ArrayList<HttpDataService?>()
        dataToBeSent.add(data)
        val response = config.webServices.sendPreyHttpData(context, dataToBeSent)
        val finalStatus = if (response.statusCode == HttpURLConnection.HTTP_OK) STATUS_STOPPED else STATUS_FAILED
        config.addActions(UtilJson.makeJsonResponse(CMD_GET, TARGET, finalStatus))
    }

    /**
     * Converts a [Location] object into an [HttpDataService] object prepared
     */
    fun convertData(location: Location?): HttpDataService? {
        if (location == null) return null
        val data = HttpDataService("location")
        data.isList = true
        val parametersMap = HashMap<String?, String?>()
        parametersMap[LocationUtil.LAT] = location.latitude.toString()
        parametersMap[LocationUtil.LNG] = location.longitude.toString()
        parametersMap[LocationUtil.ACC] = location.accuracy.roundToInt().toFloat().toString()
        parametersMap[LocationUtil.METHOD] = "native"
        data.addDataListAll(parametersMap)
        PreyLogger.d("lat:${location.latitude} lng:${location.longitude} acc:${location.accuracy}")
        return data
    }

}