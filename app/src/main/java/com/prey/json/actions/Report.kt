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
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPhone
import com.prey.PreyPhoneKt
import com.prey.actions.picture.PictureUtil
import com.prey.actions.report.ReportScheduled
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServices
import com.prey.net.UtilConnection
import com.prey.services.ReportJobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * Handles the generation and transmission of device status reports.
 *
 * This class is responsible for managing the "missing" state of the device. It can be
 * commanded to start (`get`) or stop (`stop`) generating periodic reports.
 *
 * When started, it collects various pieces of information:
 * - GPS location.
 * - Wi-Fi network information (active and nearby access points).
 * - A picture from the front camera and a screenshot of the current screen.
 *
 * This data is then compiled into a multipart/form-data request and sent to the
 * Prey servers. It also schedules future reports using `ReportScheduled` and `ReportJobService`.
 *
 * The `stop` command cancels any scheduled reports and resets the device's "missing" status.
 *
 * This class implements [CommandTarget] to integrate with the remote command system.
 */
object Report : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> getCoroutine(context, options)
            CMD_STOP -> stop(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    fun getCoroutine(context: Context, options: JSONObject) {
        scope.launch { get(context, options) }
    }

    /**
     * Initiates the device reporting process.
     *
     * This function is triggered when a "get" command is received. It configures and starts
     * the periodic reporting mechanism based on the provided options. It sets the device state
     * to "missing", configures the report interval and any exclusions, and then schedules
     * the report generation and submission. An immediate report is also triggered.
     *
     * The options JSON object can contain:
     * - "interval": An integer specifying the time in minutes between reports. Defaults to 10 if not provided or invalid.
     * - "exclude": A string containing a comma-separated list of data types to exclude from the report.
     *
     * @param context The application context.
     * @param options A JSONObject containing configuration parameters for the reporting process.
     */
    suspend fun get(context: Context, options: JSONObject) {
        PreyLogger.d("Report get options:${options}")
        val lastReportStartDate = Date().getTime()
        PreyLogger.d("Report lastReportStartDate:${lastReportStartDate}")
        val config = PreyConfig.getPreyConfig(context)
        config.lastReportStartDate = lastReportStartDate
        config.isMissing = true
        val interval = options.optInt("interval", 10)
        PreyLogger.d("Report interval:${interval}")
        val exclude = options.optString("exclude", "")
        PreyLogger.d("Report exclude:${exclude}")
        config.intervalReport = "$interval"
        config.excludeReport = exclude
        config.removeTimeNextReport()
        ReportScheduled.getInstance(context).run()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ReportJobService.schedule(context)
        }
        delay(2000)
        // Send the report to the server using the PreyWebServices instance
        startReport(context)
    }

    /**
     * Stops the device reporting process and resets the device's missing status.
     *
     * This function is triggered when a "stop" command is received. It performs the following actions:
     * - Resets the scheduled reporting tasks via [ReportScheduled].
     * - Updates the [PreyConfig] to mark the device as no longer missing.
     * - Clears the reporting interval and exclusion settings.
     * - Cancels any background jobs currently managed by [ReportJobService].
     *
     * @param context The application context.
     * @param options A JSONObject containing configuration parameters (currently used for logging).
     */
    fun stop(context: Context, options: JSONObject) {
        PreyLogger.d("Report stop options:${options}")
        ReportScheduled.getInstance(context).reset()
        val config = PreyConfig.getPreyConfig(context)
        config.isMissing = false
        config.intervalReport = ""
        config.excludeReport = ""
        ReportJobService.cancel(context)
    }

    fun startReportCoroutine(context: Context) {
        scope.launch { startReport(context) }
    }

    /**
     * Gathers device data and sends a multipart report to the Prey server.
     *
     * This function executes on [Dispatchers.IO] and performs the following steps:
     * 1. Fetches current configuration and exclusion settings.
     * 2. Collects available data based on allowed categories:
     *    - **Location**: Latitude, longitude, and accuracy via GPS/Network.
     *    - **Active Access Point**: SSID and connection details of the current Wi-Fi.
     *    - **Access Points List**: A scan of all nearby Wi-Fi networks.
     *    - **Pictures**: Captures from the camera and device screenshots.
     * 3. Constructs a `multipart/form-data` POST request.
     * 4. Sends the data to the Prey web service with appropriate authorization and headers.
     *
     * Errors during data collection or transmission are caught and logged via [PreyLogger].
     *
     * @param context The application context used to access system services and configuration.
     */
    suspend fun startReport(context: Context) = withContext(Dispatchers.IO) {
        try {
            val config = PreyConfig.getPreyConfig(context)
            val isMissing = config.isMissing
            PreyLogger.d("startReport isMissing:${isMissing}")
            if (!config.isMissing) {
                return@withContext
            }
            val exclude = config.excludeReport
            PreyLogger.d("startReport exclude:${exclude}")
            val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            //Location with safe handling of nulls
            if (!exclude.contains("location")) {
                getLocation(context)?.let { location ->
                    PreyLogger.d("startReport lat:${location.latitude} lng:${location.longitude}")
                    val accuracy = (location.accuracy * 100.0).roundToInt() / 100.0
                    multipart.addFormDataPart("location[lat]", location.latitude.toString())
                    multipart.addFormDataPart("location[lng]", location.longitude.toString())
                    multipart.addFormDataPart("location[method]", "native")
                    multipart.addFormDataPart("location[accuracy]", accuracy.toString())
                }
            }
            val listWifi = PreyPhoneKt.getListWifi(context)
            //Active access point
            if (!exclude.contains("active_access_point")) {
                PreyLogger.d("startReport active_access_point")
                PreyPhoneKt.getWifi(context)?.let { wifiInfo ->
                    multipart.addFormDataPart("active_access_point[ssid]", "{${wifiInfo.ssid}}")
                    listWifi.find { it.SSID == wifiInfo.ssid }?.let { scan ->
                        addWifiToMultipart(multipart, "active_access_point", scan)
                    }
                }
            }
            //List of access points
            if (!exclude.contains("access_points_list")) {
                PreyLogger.d("startReport access_points_list")
                listWifi.forEachIndexed { index, scan ->
                    addWifiToMultipart(multipart, "access_points_list[$index]", scan)
                }
            }
            //Images (Avoiding IndexOutOfBoundsException)
            if (!exclude.contains("picture")) {
                PreyLogger.d("startReport picture")
                val picture = PictureUtil.getPicture(context)
                val mediaType = "image/*".toMediaType()
                picture.entityFiles.getOrNull(0)?.let { entity ->
                    multipart.addFormDataPart("picture", "picture.png", RequestBody.create(mediaType, entity.bytes))
                }
                picture.entityFiles.getOrNull(1)?.let { entity ->
                    multipart.addFormDataPart("screenshot", "screenshot.png", RequestBody.create(mediaType, entity.bytes))
                }
            }
            val url = PreyWebServices.getInstance().getReportUrlJson(context)
            PreyLogger.d("startReport url:${url}")
            //Request Configuration
            val request = Request.Builder()
                .url(url)
                .post(multipart.build())
                .addHeader("User-Agent", UtilConnection.getUserAgent(config))
                .addHeader("Origin", "android:com.prey")
                .addHeader("Authorization", UtilConnection.getAuthorization(config))
                .build()
            //Execution
            val client = OkHttpClient()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    PreyLogger.d("Error in report: ${response.code}")
                } else {
                    PreyLogger.d("Report sent: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error in startReport: ${e.message}", e)
        }
    }

    //Auxiliary function to avoid repeating WiFi code
    private fun addWifiToMultipart(builder: MultipartBody.Builder, prefix: String, scan: ScanResult) {
        builder.addFormDataPart("$prefix[ssid]", scan.SSID)
        builder.addFormDataPart("$prefix[security]", scan.capabilities)
        builder.addFormDataPart("$prefix[mac_address]", scan.BSSID)
        builder.addFormDataPart("$prefix[signal_strength]", scan.level.toString())
        val channelIndex = PreyPhone.channelsFrequency.indexOf(scan.frequency)
        builder.addFormDataPart("$prefix[channel]", channelIndex.toString())
    }

    private lateinit var fusedClient: FusedLocationProviderClient

    /**
     * Asynchronously retrieves the current high-accuracy location of the device.
     *
     * This function uses the Fused Location Provider to request a single location update.
     * It wraps the callback-based API in a [suspendCancellableCoroutine] to provide a
     * synchronous-style call within a coroutine.
     *
     * It requires [Manifest.permission.ACCESS_FINE_LOCATION] to be granted. If the
     * permission is missing, the function will not execute the request and will
     * return `null` (or time out if not handled).
     *
     * @param context The application context used to check permissions and initialize the location client.
     * @return The current [Location] object, or `null` if the location cannot be retrieved
     *         or permissions are denied.
     */
    private suspend fun getLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                0
            ).setMaxUpdates(1).build()
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            fusedClient.removeLocationUpdates(this)
                            cont.resume(result.lastLocation)
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        }

}