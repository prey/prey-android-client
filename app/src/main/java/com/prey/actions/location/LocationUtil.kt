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

object LocationUtil {
    const val LAT: String = "lat"
    const val LNG: String = "lng"
    const val ACC: String = "accuracy"
    const val METHOD: String = "method"

    @JvmOverloads
    fun dataLocation(
        ctx: Context,
        messageId: String?,
        asynchronous: Boolean,
        maximum: Int = MAXIMUM_OF_ATTEMPTS
    ): HttpDataService? {
        var data: HttpDataService? = null
        try {
            val preyLocation = getLocation(ctx, messageId, asynchronous, maximum)
            if (preyLocation != null && (preyLocation.getLat() != 0.0 && preyLocation.getLng() != 0.0)) {
                PreyLogger.d(
                    String.format(
                        "locationData:%s %s %s",
                        preyLocation.getLat(),
                        preyLocation.getLng(),
                        preyLocation.getAccuracy()
                    )
                )
                PreyConfig.getInstance(ctx).setLocation(preyLocation)
                PreyLocationManager.getInstance().setLastLocation(preyLocation)
                data = convertData(preyLocation)
            } else {
                PreyLogger.d("locationData else:")
                return null
            }
        } catch (e: Exception) {
            sendNotify(ctx, "Error", messageId)
        }
        return data
    }

    @Throws(Exception::class)
    fun getLocation(ctx: Context, messageId: String?, asynchronous: Boolean): PreyLocation? {
        return getLocation(ctx, messageId, asynchronous, MAXIMUM_OF_ATTEMPTS)
    }

    @Throws(Exception::class)
    fun getLocation(
        ctx: Context,
        messageId: String?,
        asynchronous: Boolean,
        maximum: Int
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        val isGpsEnabled = PreyLocationManager.getInstance().isGpsLocationServiceActive(ctx)
        val isNetworkEnabled = PreyLocationManager.getInstance().isNetworkLocationServiceActive(ctx)
        val isWifiEnabled = PreyWifiManager.getInstance().isWifiEnabled(ctx)
        val isGooglePlayServicesAvailable = PreyUtils.isGooglePlayServicesAvailable(ctx)
        val json = JSONObject()
        try {
            json.put("gps", isGpsEnabled)
            json.put("net", isNetworkEnabled)
            json.put("wifi", isWifiEnabled)
            json.put("play", isGooglePlayServicesAvailable)
        } catch (e: JSONException) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        val locationInfo = json.toString()
        PreyConfig.getInstance(ctx).setLocationInfo(locationInfo)
        PreyLogger.d(locationInfo)
        val method = getMethod(isGpsEnabled, isNetworkEnabled)
        try {
           // preyLocation = getPreyLocationAppService(ctx, method, asynchronous, preyLocation, maximum)
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error PreyLocationApp:%s", e.message), e)
        }
        try {
           if (preyLocation?.getLocation() == null || (preyLocation.getLat() == 0.0 && preyLocation.getLng() == 0.0)) {
                preyLocation =
                    getPreyLocationAppServiceOreo(ctx, method, asynchronous, preyLocation)
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error AppServiceOreo:%s", e.message), e)
        }
        /*
        if (!isGooglePlayServicesAvailable && (preyLocation == null || preyLocation.getLocation() == null || (preyLocation.getLat() == 0.0 && preyLocation.getLng() == 0.0))) {
            val listWifi = PreyPhone(ctx).getListWifi()
            preyLocation = PreyWebServices.getInstance().getLocationWithWifi(ctx, listWifi)
        }*/
        if (preyLocation != null) {
            PreyLogger.d(
                String.format(
                    "preyLocation lat:%s lng:%s acc:%s",
                    preyLocation.getLat(),
                    preyLocation.getLng(),
                    preyLocation.getAccuracy()
                )
            )
        }
        return preyLocation
    }

    private fun isGooglePlayServicesAvailable(ctx: Context): Boolean {
        var isGooglePlayServicesAvailable = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx)
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

    private fun sendNotify(ctx: Context, message: String) {
        val parms = UtilJson.makeMapParam("get", "location", "failed", message)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, parms)
    }

    private fun sendNotify(ctx: Context, message: String, status: String?) {
        val parms = UtilJson.makeMapParam("get", "location", status, message)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, parms)
    }

    const val MAXIMUM_OF_ATTEMPTS: Int = 5
    const val MAXIMUM_OF_ATTEMPTS2: Int = 2
    val SLEEP_OF_ATTEMPTS: IntArray = intArrayOf(1, 1, 1, 1,1,1,1, 1, 1, 1,1,1,1, 1, 1, 1,1,1)

    @Throws(Exception::class)
    fun getPreyLocationPlayService(
        ctx: Context,
        method: String,
        asynchronous: Boolean,
        preyLocationOld: PreyLocation?
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        PreyLogger.d("getPreyLocationPlayService")
        val play = PreyGooglePlayServiceLocation()
        try {
            Thread { play.init(ctx) }.start()
            var currentLocation: Location? = null
            val manager = PreyLocationManager.getInstance()
            currentLocation = play.getLastLocation(ctx)
            if (currentLocation != null) {
                PreyLogger.d(String.format("currentLocation:%s", currentLocation.toString()))
                preyLocation = PreyLocation(currentLocation, method)
            }
        } catch (e: Exception) {
            PreyLogger.d(String.format("Error getPreyLocationPlayService:%s", e.message))
            throw e
        } finally {
            if (play != null) play.stopLocationUpdates()
        }
        return preyLocation
    }

    fun getPreyLocationAppServiceOreo(
        ctx: Context,
        method: String?,
        asynchronous: Boolean,
        preyLocationOld: PreyLocation?
    ): PreyLocation? {

        var preyLocation: PreyLocation? = null
        val intentLocation = Intent(ctx, LocationUpdatesService::class.java)
        try {
            //ctx.startService(intentLocation)
            var i = 0
            while (i <1){// MAXIMUM_OF_ATTEMPTS2) {
                PreyLogger.d(String.format("getPreyLocationAppServiceOreo[%d]:", i))
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
            PreyLogger.e(String.format("Error getPreyLocationAppServiceOreo:%s", e.message), e)
            throw e
        } finally {
          //  ctx.stopService(intentLocation)
        }
        return preyLocation
    }

    @Throws(Exception::class)
    private fun getPreyLocationAppService(
        ctx: Context,
        method: String,
        asynchronous: Boolean,
        preyLocationOld: PreyLocation?,
        maximum: Int
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
            (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val intentLocation = Intent(ctx, LocationService::class.java)
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startService(intentLocation)
                } else {
                    ctx.startService(intentLocation)
                }
                preyLocation = waitLocation(ctx, method, asynchronous, maximum)
            } catch (e: Exception) {
                PreyLogger.e(String.format("getPreyLocationAppService e:%s", e.message), e)
                throw e
            } finally {
                ctx.stopService(intentLocation)
            }
        }
        return preyLocation
    }

    fun waitLocation(
        ctx: Context?,
        method: String?,
        asynchronous: Boolean,
        maximum: Int
    ): PreyLocation? {
        var preyLocation: PreyLocation? = null
        val location = PreyLocationManager.getInstance().getLastLocation()
        if (location != null && location.isValid()) {
            preyLocation = location
            preyLocation.setMethod(method)
            PreyLogger.d(String.format("getPreyLocationAppService:%s", preyLocation.toString()))
        }
        return preyLocation
    }

    private fun sendLocation(
        ctx: Context,
        asynchronous: Boolean,
        locationOld: PreyLocation?,
        locationNew: PreyLocation?
    ): PreyLocation? {
        val distance = distance(locationOld, locationNew)
        val distanceLocation = PreyConfig.getInstance(ctx).getDistanceLocation().toDouble()
        if (locationNew != null) {
            if (locationOld == null || distance > distanceLocation || locationOld.getAccuracy() > locationNew.getAccuracy()) {
                if (asynchronous) {
                    val data = convertData(locationNew)
                    val dataToBeSent = ArrayList<HttpDataService>()
                    dataToBeSent.add(data!!)
                    PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent)
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
        val parametersMap = HashMap<String, String?>()
        parametersMap[LAT] = lastLocation.getLat().toString()
        parametersMap[LNG] = lastLocation.getLng().toString()
        parametersMap[ACC] = Math.round(lastLocation.getAccuracy()).toString()
        parametersMap[METHOD] = lastLocation.getMethod()
        data.addDataListAll(parametersMap)
        PreyLogger.d(
            String.format(
                "lat:%.2f lng:%.2f acc:%.2f method:%s",
                lastLocation.getLat(),
                lastLocation.getLng(),
                lastLocation.getAccuracy(),
                lastLocation.getMethod()
            )
        )
        return data
    }

    fun dataPreyLocation(ctx: Context, messageId: String?): PreyLocation {
        val data = dataLocation(ctx, messageId, false)
        val location = PreyLocation()

        location.setLat(data!!.getDataList()!!.get(LAT)!!.toDouble())
        location.setLng(data.getDataList()!!.get(LNG)!!.toDouble())
        location.setAccuracy(data.getDataList()!!.get(ACC)!!.toFloat())
        return location
    }
}