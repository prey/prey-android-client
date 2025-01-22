/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import android.provider.Settings
import com.prey.actions.aware.kotlin.AwareController
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.location.kotlin.LocationUtil
import com.prey.actions.location.kotlin.PreyLocation
import com.prey.actions.location.kotlin.PreyLocationManager
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.json.kotlin.JsonAction
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPhone
import com.prey.net.kotlin.PreyWebServices
import org.json.JSONObject

class Location : JsonAction() {
    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("Ejecuting Location Report.")
        PreyLocationManager.getInstance().setLastLocation(null)
        val listResult = super.report(ctx, list, parameters)
        return listResult
    }

    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("Ejecuting Location Get.")
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d(String.format("jobId:%s", jobId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s" + e.message), e)
        }
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        PreyLocationManager.getInstance().setLastLocation(null)
        PreyConfig.getInstance(ctx).setLocation(null)
        PreyConfig.getInstance(ctx).setLocationInfo("")
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            ctx,
            "processed",
            messageId,
            UtilJson.makeMapParam("get", "location", "started", reason)
        )
        PreyLogger.d(javaClass.name)
        var i = 0
        val maximum = LocationUtil.MAXIMUM_OF_ATTEMPTS
        var data: HttpDataService? = null
        var dataToBeSent: ArrayList<HttpDataService>? = null
        var accuracy = -1f
        var send: Boolean
        var first = true
        val isAirplaneModeOn: Boolean = PreyPhone.getInstance(ctx)!!.isAirplaneModeOn(ctx)
        PreyLogger.d(String.format("Location get isAirplaneModeOn:%s", isAirplaneModeOn))
        while (i < maximum && !isAirplaneModeOn) {
            send = false
            try {
                LocationUtil.dataLocation(ctx, messageId, true)
                val location: PreyLocation = PreyConfig.getInstance(ctx).getLocation()
                if (location != null) {
                    data = LocationUtil.convertData(location)
                    val acc = data!!.getDataListKey(LocationUtil.ACC)
                    if (acc != null && acc != "") {
                        var newAccuracy = 0f
                        try {
                            newAccuracy = acc.toFloat()
                            PreyLogger.d(
                                String.format(
                                    "accuracy_:%s newAccuracy:%s",
                                    accuracy,
                                    newAccuracy
                                )
                            )
                        } catch (e: Exception) {
                            PreyLogger.e(String.format("Error:%s", e.message), e)
                        }
                        if (newAccuracy > 0) {
                            if (accuracy == -1f || accuracy > newAccuracy) {
                                send = true
                                accuracy = newAccuracy
                            }
                        }
                    }
                    if (send) {
                        //It is added if it is the first time the location is sent
                        val dataToast = HttpDataService("skip_toast")
                        dataToast.setList(false)
                        dataToast.setKeyValue("skip_toast")
                        dataToast.setSingleData((!first).toString())

                        dataToBeSent = ArrayList()

                        dataToBeSent.add(data)
                        dataToBeSent.add(dataToast)
                        PreyLogger.d(String.format("send [%s]:%s", i, accuracy))
                        PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent)
                        first = false
                        i = LocationUtil.MAXIMUM_OF_ATTEMPTS
                    }
                }
                if (i < maximum) {
                    try {
                        Thread.sleep((LocationUtil.SLEEP_OF_ATTEMPTS[i] * 1000).toLong())
                    } catch (e: Exception) {
                        i = LocationUtil.MAXIMUM_OF_ATTEMPTS
                        break
                    }
                }
            } catch (e: Exception) {
                i = LocationUtil.MAXIMUM_OF_ATTEMPTS
                break
            }
            i++
        }
        if (data == null) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                "failed",
                messageId,
                UtilJson.makeMapParam(
                    "get",
                    "location",
                    "failed",
                    PreyConfig.getInstance(ctx).getLocationInfo()
                )
            )
        } else {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                "processed",
                messageId,
                UtilJson.makeMapParam("get", "location", "stopped", reason)
            )
        }
        try {
            val nameDevice = Settings.Secure.getString(ctx.contentResolver, "bluetooth_name")
            if (nameDevice != null && "" != nameDevice) {
                PreyLogger.d(String.format("nameDevice: %s", nameDevice))
                PreyWebServices.getInstance().sendPreyHttpDataName(ctx, nameDevice)
                val nameDeviceInfo = PreyWebServices.getInstance().getNameDevice(ctx)
                if (nameDeviceInfo != null && "" != nameDeviceInfo) {
                    PreyLogger.d(String.format("nameDeviceInfo: %s", nameDeviceInfo))
                    PreyConfig.getInstance(ctx).setDeviceName(nameDeviceInfo)
                }
            }
        } catch (e: Exception) {
            PreyLogger.d(String.format("Data wasn't send: %s", e.message))
        }
        return dataToBeSent
    }

    fun start(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("Ejecuting Location Start.")
        val listResult = super.get(ctx!!, list, parameters)
        return listResult
    }

    override fun run(ctx: Context, list: MutableList<ActionResult>?, parameters: JSONObject?): HttpDataService? {
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        val data = LocationUtil.dataLocation(ctx, messageId, false)
        return data
    }



    fun start_location_aware(
        ctx: Context,
        list: MutableList<ActionResult?>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("AWARE start_location_aware:")
        AwareController.getInstance().init(ctx)
        return null
    }

    companion object {
        const val DATA_ID: String = "geo"
    }
}