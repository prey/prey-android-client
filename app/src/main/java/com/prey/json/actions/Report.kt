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
import com.prey.actions.picture.PictureUtil
import com.prey.actions.report.ReportScheduled
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServices
import com.prey.net.UtilConnection
import com.prey.services.ReportJobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
import kotlin.collections.forEach
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
class Report : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> getCoroutine(context, options)
            CMD_STOP -> stop(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    fun getCoroutine(context: Context, options: JSONObject){
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
        val interval = if (options.has("interval")) options.getInt("interval") else 10
        PreyLogger.d("Report interval:${interval}")
        val exclude = if (options.has("exclude")) options.getString("exclude") else ""
        PreyLogger.d("Report exclude:${exclude}")
        config.setIntervalReport("$interval")
        config.excludeReport = exclude
        config.removeTimeNextReport()
        ReportScheduled.getInstance(context).run()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ReportJobService.schedule(context)
        }
        // Send the report to the server using the PreyWebServices instance
        startReport(context, JSONObject())
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

    fun startReportCoroutine(context: Context, options: JSONObject){
        scope.launch { startReport(context, options) }
    }
    suspend fun startReport(context: Context, options: JSONObject) {
        try {
            val listWifi = listWifi(context)
            val wifiInfo = getWifi(context)
            val location = getLocation(context)
            var accuracy = location?.accuracy!!.toDouble()
            accuracy = (accuracy * 100.0).roundToInt() / 100.0
            PreyLogger.d("latitude:${location?.latitude}")
            PreyLogger.d("longitude:${location?.longitude}")
            PreyLogger.d("accuracy:${accuracy}")
            val picture = PictureUtil.getPicture(context)
            PreyLogger.d("report3 2")
            val entity1 = picture.entityFiles.get(0)
            val entity2 = picture.entityFiles.get(1)
            val mediaType = "image/*".toMediaType()
            val multipart = MultipartBody.Builder()
            multipart.setType(MultipartBody.FORM)
            multipart.addFormDataPart("location[lat]", "${location?.latitude}")
            multipart.addFormDataPart("location[lng]", "${location?.longitude}")
            multipart.addFormDataPart("location[method]", "native")
            multipart.addFormDataPart("location[accuracy]", "${accuracy}")
            if (wifiInfo != null) {
                multipart.addFormDataPart("active_access_point[ssid]", "{${wifiInfo.ssid}}")
                listWifi.forEach { scan ->
                    if (scan.SSID == wifiInfo.ssid) {
                        multipart.addFormDataPart(
                            "active_access_point[security]",
                            "{${scan.capabilities}}"
                        )
                        multipart.addFormDataPart(
                            "active_access_point[mac_address]",
                            "{${scan.BSSID}}"
                        )
                        multipart.addFormDataPart(
                            "active_access_point[signal_strength]",
                            "{${scan.level}}"
                        )
                        multipart.addFormDataPart(
                            "active_access_point[channel]",
                            "{${PreyPhone.channelsFrequency.indexOf(Integer.valueOf(scan.frequency))}}"
                        )
                    }
                }
            }
            var i = 0
            listWifi.forEach { scan ->
                multipart.addFormDataPart("access_points_list[${i}][ssid]", "${scan.SSID}");
                multipart.addFormDataPart(
                    "access_points_list[${i}][security]",
                    "${scan.capabilities}"
                );
                multipart.addFormDataPart(
                    "access_points_list[${i}][mac_address]",
                    "${scan.BSSID}"
                );
                multipart.addFormDataPart(
                    "access_points_list[${i}][signal_strength]",
                    "${scan.level}"
                );
                multipart.addFormDataPart(
                    "access_points_list[${i}][channel]",
                    "${PreyPhone.channelsFrequency.indexOf(Integer.valueOf(scan.frequency))}"
                );
                i++
            }
            if (entity1 != null) {
                multipart.addFormDataPart(
                    "picture",
                    "picture.png",
                    RequestBody.create(mediaType, entity1.bytes)
                )
            }
            if (entity2 != null) {
                multipart.addFormDataPart(
                    "screenshot",
                    "screenshot.png",
                    RequestBody.create(mediaType, entity2.bytes)
                )
            }
            val body = multipart.build()
            val preyConfig = PreyConfig.getPreyConfig(context)
            val authorization = UtilConnection.getAuthorization(preyConfig)
            val userAgent = UtilConnection.getUserAgent(preyConfig)
            val request = Request.Builder()
                .url(PreyWebServices.getInstance().getReportUrlJson(context))
                .post(body)
                .addHeader("User-Agent", userAgent)
                .addHeader("Origin", "android:com.prey")
                .addHeader("Authorization", authorization)
                .build()
            PreyLogger.d("report 3")
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    PreyLogger.e("Error: ${e.message}", e)
                }
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            PreyLogger.d("Unexpected code $response")
                        } else {
                            PreyLogger.d("Report Response: ${response.body?.string()}")
                        }
                    }
                }
            })
        } catch (e: Exception) {
            PreyLogger.e("Unexpected code ${e.message}", e)
        }
    }

    private lateinit var fusedClient: FusedLocationProviderClient
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

    private suspend fun listWifi(context: Context): List<ScanResult> {
        var listScanResults: List<ScanResult> = emptyList()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val wifiMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            listScanResults = wifiMgr.scanResults
        }
        return listScanResults;
    }

    private suspend fun getWifi(context: Context): WifiInfo? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val wifiMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiMgr.connectionInfo
        }
        return null
    }

}