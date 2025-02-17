/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build

import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil

import com.prey.actions.HttpDataService
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.managers.PreyWifiManager
import com.prey.net.PreyWebServices
import com.prey.services.LocationService

import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

object LocationUtil {

    @JvmOverloads
    fun dataLocation(
        context: Context,
        messageId: String?,
        asynchronous: Boolean,
        maximum: Int = MAXIMUM_OF_ATTEMPTS
    ): HttpDataService? {
        var data: HttpDataService? = null
        try {
            val preyLocation = getLocation(context, messageId, asynchronous, maximum)
            if (preyLocation != null && (preyLocation.getLat() != 0.0 && preyLocation.getLng() != 0.0)) {
                PreyLogger.d("locationData:${preyLocation.getLat()} ${preyLocation.getLng()} ${preyLocation.getAccuracy()}")
                PreyConfig.getInstance(context).setLocation(preyLocation)
                PreyLocationManager.getInstance().setLastLocation(preyLocation)
                data = convertData(preyLocation)
            } else {
                PreyLogger.d("locationData else:")
                return null
            }
        } catch (e: Exception) {
            sendNotify(context, "Error", messageId)
        }
        return data
    }

    @Throws(Exception::class)
    fun getLocation(context: Context, messageId: String?, asynchronous: Boolean): PreyLocation? {
        return getLocation(context, messageId, asynchronous, MAXIMUM_OF_ATTEMPTS)
    }

    @Throws(Exception::class)
    fun getLocation(
        context: Context,
        messageId: String?,
        asynchronous: Boolean,
        maximum: Int
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        val isGpsEnabled = PreyLocationManager.getInstance().isGpsLocationServiceActive(context)
        val isNetworkEnabled = PreyLocationManager.getInstance().isNetworkLocationServiceActive(context)
        val isWifiEnabled = PreyWifiManager.getInstance().isWifiEnabled(context)
        val isGooglePlayServicesAvailable = PreyUtils.isGooglePlayServicesAvailable(context)
        val json = JSONObject()
        try {
            json.put("gps", isGpsEnabled)
            json.put("net", isNetworkEnabled)
            json.put("wifi", isWifiEnabled)
            json.put("play", isGooglePlayServicesAvailable)
        } catch (e: JSONException) {
            PreyLogger.e("Error:${e.message}", e)
        }
        val locationInfo = json.toString()
        PreyConfig.getInstance(context).setLocationInfo(locationInfo)
        PreyLogger.d(locationInfo)
        val method = getMethod(isGpsEnabled, isNetworkEnabled)
        try {
            // preyLocation = getPreyLocationAppService(context, method, asynchronous, preyLocation, maximum)
        } catch (e: Exception) {
            PreyLogger.e("Error PreyLocationApp:${e.message}", e)
        }
        try {
            if (preyLocation?.getLocation() == null || (preyLocation.getLat() == 0.0 && preyLocation.getLng() == 0.0)) {
                preyLocation =
                    getPreyLocationAppServiceOreo(context, method, asynchronous, preyLocation)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error AppServiceOreo:${e.message}", e)
        }
        if (preyLocation != null) {
            PreyLogger.d("preyLocation lat:${preyLocation.getLat()} lng:${preyLocation.getLng()} acc:${preyLocation.getAccuracy()}")
        }
        return preyLocation
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        var isGooglePlayServicesAvailable = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)
            if (ConnectionResult.SUCCESS == resultCode) {
                isGooglePlayServicesAvailable = true
            }
        }
        return isGooglePlayServicesAvailable
    }

    private fun getMethod(isGpsEnabled: Boolean, isNetworkEnabled: Boolean): String {
        if (isGpsEnabled && isNetworkEnabled) {
            return "native"
        }
        if (isGpsEnabled) {
            return "gps"
        }
        if (isNetworkEnabled) {
            return "network"
        }
        return ""
    }

    private fun sendNotify(context: Context, message: String) {
        val parameters = UtilJson.makeMapParam("get", "location", "failed", message)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(context, parameters)
    }

    private fun sendNotify(context: Context, message: String, status: String?) {
        val parameters = UtilJson.makeMapParam("get", "location", status, message)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(context, parameters)
    }

    const val MAXIMUM_OF_ATTEMPTS: Int = 5
    const val MAXIMUM_OF_ATTEMPTS2: Int = 2
    val SLEEP_OF_ATTEMPTS: IntArray =
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)

    @Throws(Exception::class)
    fun getPreyLocationPlayService(
        context: Context,
        method: String,
        asynchronous: Boolean,
        preyLocationOld: PreyLocation?
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        PreyLogger.d("getPreyLocationPlayService")
        val play = PreyGooglePlayServiceLocation()
        try {
            Thread { play.init(context) }.start()
            var currentLocation: Location? = null
            val manager = PreyLocationManager.getInstance()
            currentLocation = play.getLastLocation(context)
            if (currentLocation != null) {
                PreyLogger.d("currentLocation:${currentLocation.toString()}")
                preyLocation = PreyLocation(currentLocation, method)
            }
        } catch (e: Exception) {
            PreyLogger.d("Error getPreyLocationPlayService:${e.message}")
            throw e
        } finally {
            if (play != null) play.stopLocationUpdates()
        }
        return preyLocation
    }

    fun getPreyLocationAppServiceOreo(
        context: Context,
        method: String?,
        asynchronous: Boolean,
        preyLocationOld: PreyLocation?
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        val intentLocation = Intent(context, LastLocationService::class.java)
        try {
            //context.startService(intentLocation)
            var i = 0
            while (i < 1) {// MAXIMUM_OF_ATTEMPTS2) {
                PreyLogger.d("getPreyLocationAppServiceOreo[${i}]")
                try {
                    Thread.sleep((SLEEP_OF_ATTEMPTS[i] * 1000).toLong())
                } catch (e: Exception) {
                    i = MAXIMUM_OF_ATTEMPTS2
                    break
                }
                preyLocation = PreyLocationManager.getInstance().getLastLocation()
                if (preyLocation != null) {
                    preyLocation.setMethod(method)
                    break
                }
                i++
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            throw e
        } finally {
            //  context.stopService(intentLocation)
        }
        return preyLocation
    }

    @Throws(Exception::class)
    private fun getPreyLocationAppService(
        context: Context,
        method: String,
        asynchronous: Boolean,
        preyLocationOld: PreyLocation?,
        maximum: Int
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
            (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val intentLocation = Intent(context, LocationService::class.java)
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startService(intentLocation)
                } else {
                    context.startService(intentLocation)
                }
                preyLocation = waitLocation(context, method, asynchronous, maximum)
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
                throw e
            } finally {
                context.stopService(intentLocation)
            }
        }
        return preyLocation
    }

    fun waitLocation(
        context: Context?,
        method: String?,
        asynchronous: Boolean,
        maximum: Int
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        val location = PreyLocationManager.getInstance().getLastLocation()
        if (location != null && location.isValid()) {
            preyLocation = location
            preyLocation.setMethod(method)
            PreyLogger.d("getPreyLocationAppService:${preyLocation.toString()}")
        }
        return preyLocation
    }

    private fun sendLocation(
        context: Context,
        asynchronous: Boolean,
        locationOld: PreyLocation?,
        locationNew: PreyLocation?
    ): PreyLocation? {
        val distance = distance(locationOld, locationNew)
        val distanceLocation = PreyConfig.getInstance(context).getDistanceLocation().toDouble()
        if (locationNew != null) {
            if (locationOld == null || distance > distanceLocation || locationOld.getAccuracy() > locationNew.getAccuracy()) {
                if (asynchronous) {
                    val data = convertData(locationNew)
                    val dataToBeSent = ArrayList<HttpDataService>()
                    dataToBeSent.add(data!!)
                    PreyWebServices.getInstance().sendPreyHttpData(context, dataToBeSent)
                }
            }
            return locationNew
        } else {
            return locationOld
        }
    }

    fun distance(locationOld: PreyLocation?, locationNew: PreyLocation?): Double {
        if (locationOld != null && locationNew != null) {
            val locStart = Location("")
            locStart.latitude = locationNew.getLat()
            locStart.longitude = locationNew.getLng()
            val locEnd = Location("")
            locEnd.latitude = locationOld.getLat()
            locEnd.longitude = locationOld.getLng()
            return Math.round(locStart.distanceTo(locEnd)).toDouble()
        } else {
            return 0.0
        }
    }

    fun convertData(lastLocation: PreyLocation?): HttpDataService? {
        if (lastLocation == null) return null
        val data = HttpDataService("location")
        data.setList(true)
        val latitude = lastLocation.getLat().toString()
        val longitude = lastLocation.getLng().toString()
        val accuracy = Math.round(lastLocation.getAccuracy()).toString()
        val method = lastLocation.getMethod()
        val parametersMap = HashMap<String, String?>()
        parametersMap[LAT] = latitude
        parametersMap[LNG] = longitude
        parametersMap[ACC] = accuracy
        parametersMap[METHOD] = method
        data.addDataListAll(parametersMap)
        PreyLogger.d("lat:${latitude} lng:${longitude} acc:${accuracy} method:${method}")
        return data
    }

    fun dataPreyLocation(context: Context, messageId: String?): PreyLocation {
        val data = dataLocation(context, messageId, false)
        val location = PreyLocation()
        location.setLat(data!!.getDataList()!!.get(LAT)!!.toDouble())
        location.setLng(data.getDataList()!!.get(LNG)!!.toDouble())
        location.setAccuracy(data.getDataList()!!.get(ACC)!!.toFloat())
        return location
    }

    fun round(value: Double): Double {
        var finalValue = 0.0
        val df = DecimalFormat("0.000000")
        val format = df.format(value)
        try {
            finalValue = df.parse(format) as Double
        } catch (e1: Exception) {
            try {
                val finalValue2 = df.parse(format) as Long
                finalValue = finalValue2.toDouble()
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}" , e)
            }
        }
        return finalValue
    }

    const val LAT: String = "lat"
    const val LNG: String = "lng"
    const val ACC: String = "accuracy"
    const val METHOD: String = "method"
}