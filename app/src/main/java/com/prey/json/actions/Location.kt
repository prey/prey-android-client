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
import com.prey.actions.location.LastLocationService
import com.prey.actions.location.LocationUtil
import com.prey.actions.location.PreyLocation
import com.prey.actions.location.PreyLocationManager
import com.prey.actions.observer.ActionResult
import com.prey.json.JsonAction
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import com.prey.workers.PreyGetLocationWorkManager

import org.json.JSONObject


class Location : JsonAction() {
    override fun report(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        PreyLogger.d("REPORT Ejecuting Location Report.")

        val dataToBeSent = super.report(context, actionResults, parameters)
        return dataToBeSent
    }


    override fun get(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val dataToBeSent = null

        PreyLogger.d("AWARE Ejecuting Location Get.")
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var jobId: String? = null
        try {
            jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            PreyLogger.d("jobId:${jobId}")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        PreyLocationManager.getInstance().setLastLocation(null)
        PreyConfig.getInstance(context).setLocation(null)
        PreyConfig.getInstance(context).setLocationInfo("")

        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            context,
            "processed",
            messageId,
            UtilJson.makeMapParam("get", "location", "started", reason)
        )
        PreyLogger.d(javaClass.name)

        Thread(Runnable {
            //AwareController.getInstance().initLastLocation(context)
            //PreyGetLocationWorkManager.getInstance().getLocationWork(context)

            val intentLocation = Intent(context, LastLocationService::class.java)
            context.startService(intentLocation)
            context.stopService(intentLocation)
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
                PreyLogger.i("AWARE Runnable[$i] send[$send]")
                if(i==10){
                    PreyGetLocationWorkManager.getInstance().getLocationWork(context)
                }
                try {

                    val location: PreyLocation? = PreyConfig.getInstance(context).getLocation()
                    if (location != null) {
                        data = LocationUtil.convertData(location)
                        val acc = data!!.getDataListKey(LocationUtil.ACC)
                        if (acc != null && acc != "") {
                            var newAccuracy = 0f
                            try {
                                newAccuracy = acc.toFloat()
                                PreyLogger.d( "accuracy_:${accuracy} newAccuracy:${newAccuracy}"   )
                            } catch (e: java.lang.Exception) {
                                PreyLogger.e("Error:${ e.message}", e)
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
                            PreyLogger.d("send [${i}]:${accuracy}")
                            PreyWebServices.getInstance().sendPreyHttpData(context, dataToBeSent)
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
                    context,
                    "failed",
                    messageId,
                    UtilJson.makeMapParam(
                        "get",
                        "location",
                        "failed",
                        PreyConfig.getInstance(context).getLocationInfo()
                    )
                )
            } else {
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    context,
                    "processed",
                    messageId,
                    UtilJson.makeMapParam("get", "location", "stopped", reason)
                )
            }
        }).start()

        return dataToBeSent
    }


    fun start(
        context: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("Ejecuting Location Start.")
        val listResult = super.get(context, list, parameters)
        return listResult
    }

    override fun run(
        context: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        PreyLogger.d("REPORT run location______________")
        var data: HttpDataService? = null
        var messageId: String? = null
        try {
            messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
            PreyLogger.d("messageId:${messageId}")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}",  e)
        }
         data = LocationUtil.dataLocation(context, messageId, false)


        return data
    }


    fun start_location_aware(
        context: Context,
        list: MutableList<ActionResult?>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("AWARE start_location_aware:")
        AwareController.getInstance().initLastLocation(context)
        return null
    }

    companion object {
        const val DATA_ID: String = "geo"
    }
}