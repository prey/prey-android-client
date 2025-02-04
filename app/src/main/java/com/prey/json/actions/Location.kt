/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.content.Intent
import com.prey.actions.aware.AwareController
import com.prey.actions.HttpDataService
import com.prey.actions.location.LocationUpdatesService
import com.prey.actions.location.LocationUtil
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager
import com.prey.actions.observer.ActionResult
import com.prey.json.JsonAction
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import org.json.JSONObject


class Location : JsonAction() {
    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        PreyLogger.d("REPORT Ejecuting Location Report.")

        val dataToBeSent = super.report(ctx, list, parameters)
        return dataToBeSent
    }


    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val dataToBeSent = null

        PreyLogger.d("Ejecuting Location Get.")
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: java.lang.Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d(String.format("jobId:%s", jobId))
        } catch (e: java.lang.Exception) {
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

        Thread(Runnable {
        val intentLocation = Intent(ctx, LocationUpdatesService::class.java)
        ctx.startService(intentLocation)
            ctx.stopService(intentLocation)
        }).start()
        Thread(Runnable {
            var data: HttpDataService? = null
            var dataToBeSent: MutableList<HttpDataService>? = null
            var i = 0
            val maximum = 30//LocationUtil.MAXIMUM_OF_ATTEMPTS

            var accuracy = -1f
            var send = false
            var first = true

            while (i < maximum&&!send) {
                PreyLogger.i("Runnable[$i] send[$send]")
                try {

                    val location: PreyLocation? = PreyConfig.getInstance(ctx).getLocation()
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
                            } catch (e: java.lang.Exception) {
                                PreyLogger.e(String.format("Error:%s", e.message), e)
                            }
                            if (newAccuracy > 0) {
                                if (accuracy == -1f || accuracy > newAccuracy) {
                                    send = true
                                    accuracy = newAccuracy
                                }
                            }
                        }
                        if (send&&first) {
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


                    Thread.sleep(1000)


                } catch (e: java.lang.Exception) {
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
        }).start()

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

    override fun run(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        PreyLogger.d("REPORT run location______________")
        var data: HttpDataService? = null
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d(String.format("messageId:%s", messageId))
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
         data = LocationUtil.dataLocation(ctx, messageId, false)

/*
        val location: PreyLocation? = PreyConfig.getInstance(ctx).getLocation()
        if(location!=null) {
            PreyLogger.d("REPORT run location______________" + location.toString())
            if (location != null && (location.getLat() != 0.0 && location.getLng() != 0.0)) {
                PreyLogger.d(
                    String.format(
                        "locationData:%s %s %s",
                        location.getLat(),
                        location.getLng(),
                        location.getAccuracy()
                    )
                )

                data = convertData(location)
            }
        }*/
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