/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily.kotlin

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.prey.actions.location.kotlin.LocationUpdatesService
import com.prey.actions.location.kotlin.LocationUtil
import com.prey.actions.location.kotlin.PreyLocation
import com.prey.actions.location.kotlin.PreyLocationManager
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.Date

class DailyLocation {
    /**
     * Method checks if it should send a location
     *
     * @param context
     */
    fun run(context: Context) {
        val dailyLocation = PreyConfig.getInstance(context).getDailyLocation()
        val nowDailyLocation = PreyConfig.FORMAT_SDF_AWARE.format(Date())
        if (nowDailyLocation != dailyLocation) {
            PreyLocationManager.getInstance().setLastLocation(null)
            try {
                PreyLocationManager.getInstance().setLastLocation(null)
                LocationUpdatesService().startForegroundService(context)
                var preyLocation: PreyLocation? = null
                var i = 0
                while (i < LocationUtil.MAXIMUM_OF_ATTEMPTS) {
                    PreyLogger.d(String.format("DAILY getPreyLocationApp[%s]", i))
                    try {
                        Thread.sleep((LocationUtil.SLEEP_OF_ATTEMPTS[i] * 1000).toLong())
                    } catch (e: InterruptedException) {
                        PreyLogger.e(String.format("DAILY error :%s", e.message), e)
                    }
                    preyLocation = PreyLocationManager.getInstance().getLastLocation()
                    if (preyLocation != null) {
                        preyLocation.setMethod("native")
                    } else {
                        PreyLogger.d(String.format("DAILY null[%s]", i))
                    }
                    if (preyLocation != null && preyLocation.getLat() != 0.0 && preyLocation.getLng() != 0.0) {
                        break
                    }
                    i++
                }
                if (preyLocation != null && preyLocation.getLat() != 0.0 && preyLocation.getLng() != 0.0) {
                    sendLocation(context, preyLocation)
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        } else {
            PreyLogger.d("DAILY location already sent")
        }
    }

    companion object {
        /**
         * Method that sends the location
         *
         * @param context
         * @param preyLocation
         */
        @Throws(Exception::class)
        fun sendLocation(context: Context, preyLocation: PreyLocation) {
            val accD = Math.round(preyLocation.getAccuracy() * 100.0) / 100.0
            val json = JSONObject()
            var method = preyLocation.getMethod()
            if (method == null) method = "native"
            json.put("lat", preyLocation.getLat())
            json.put("lng", preyLocation.getLng())
            json.put("accuracy", accD)
            json.put("method", method)
            json.put("force", true)
            val location = JSONObject()
            location.put("location", json)
            if (Build.VERSION.SDK_INT > 9) {
                val policy = ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
            }
            val preyResponse = PreyWebServices.getInstance().sendLocation(context, location)
            if (preyResponse != null) {
                val statusCode = preyResponse.getStatusCode()
                PreyLogger.d(String.format("DAILY getStatusCode :%s", statusCode))
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
                    PreyConfig.getInstance(context).setDailyLocation(
                        PreyConfig.FORMAT_SDF_AWARE.format(
                            Date()
                        )
                    )
                }
                PreyLogger.d(String.format("DAILY sendNowAware:%s", preyLocation.toString()))
            }
        }
    }
}