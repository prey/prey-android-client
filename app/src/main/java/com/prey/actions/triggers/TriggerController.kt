/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.prey.PreyConfig

import com.prey.json.UtilJson
import com.prey.PreyLogger
import com.prey.net.UtilConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.json.JSONObject

/**
 * TriggerController is responsible for managing triggers and updating them based on data from the web.
 */
class TriggerController {
    private var listBD: List<TriggerDto>? = null
    private var listWeb: List<TriggerDto>? = null

    /**
     * Runs the trigger controller, updating triggers based on internet availability.
     *
     * @param context The application context.
     */
    fun run(context: Context) {
        try {
            if (UtilConnection.getInstance().isInternetAvailable()) {
                Thread.sleep(1000)
                val dataSource = TriggerDataSource(context)
                listBD = dataSource.allTriggers
                listWeb = null
                try {
                    listWeb = TriggerParse.getJSONFromUrl(context)
                } catch (e: Exception) {
                    PreyLogger.e("error TriggerController get json:${e.message}", e)
                }
                updateTriggers(context, listWeb, listBD, dataSource)
            }
        } catch (e: Exception) {
            PreyLogger.e("error TriggerController run:${e.message}", e)
        }
    }

    /**
     * Updates triggers based on data from the web and database.
     *
     * @param context The application context.
     * @param listWeb Triggers from the web.
     * @param listBD Triggers from the database.
     * @param dataSource Data source for triggers.
     */
    private fun updateTriggers(
        context: Context,
        listWeb: List<TriggerDto>?,
        listBD: List<TriggerDto>?,
        dataSource: TriggerDataSource
    ) {
        try {
            val listDelete: List<TriggerDto> = ArrayList()
            val listUpdate: List<TriggerDto> = ArrayList()
            val mapBD = convertMap(listBD)
            val mapWeb = convertMap(listWeb)
            val removeList: MutableList<Int> = ArrayList()
            val listRemove: MutableList<Int> = ArrayList()
            val listAdd: MutableList<TriggerDto> = ArrayList()
            val listRun: MutableList<TriggerDto> = ArrayList()
            val listDel: MutableList<TriggerDto> = ArrayList()
            val listStop: MutableList<TriggerDto> = ArrayList()
            run {
                var i = 0
                while (listBD != null && i < listBD.size) {
                    val dto = listBD[i]
                    if (mapWeb != null && !mapWeb.containsKey(dto.getId())) {
                        removeList.add(dto.getId())
                        listRemove.add(dto.getId())
                        listDel.add(dto)
                        dataSource.deleteTrigger("${dto.getId()}")
                    }
                    i++
                }
            }
            if (listDel.size > 0) {
                cancelAlarm(context, listDel)
            }
            if (removeList != null && removeList.size > 0) {
                var infoDelete = "["
                var i = 0
                while (removeList != null && i < removeList.size) {
                    infoDelete += removeList[i]
                    if (i + 1 < removeList.size) {
                        infoDelete += ","
                    }
                    i++
                }
                infoDelete += "]"
                PreyLogger.d("Trigger infoDelete:$infoDelete")
                sendNotify(
                    context,
                    UtilJson.makeMapParam("start", "triggers", "stopped", infoDelete)
                )
            }
            run {
                var i = 0
                while (listWeb != null && i < listWeb.size) {
                    val tri = listWeb[i]
                    if (mapBD!!.containsKey(tri.getId())) {
                        listRun.add(tri)
                    } else {
                        listAdd.add(tri)
                    }
                    i++
                }
            }
            run {
                var i = 0
                while (listRun != null && i < listRun.size) {
                    val trigger = listRun[i]
                    try {
                        TimeTrigger.updateTrigger(context, trigger)
                    } catch (te: TriggerException) {
                        listStop.add(trigger)
                        PreyLogger.d("TimeTrigger listRun exception id:${trigger.getId()} ,state:${te.code}")
                    }
                    i++
                }
            }
            var infoStop = "["
            run {
                var i = 0
                while (listStop != null && i < listStop.size) {
                    val trigger = listStop[i]
                    infoStop += "{\"id\":${trigger.getId()},\"state\":3}"
                    if (i + 1 < listStop.size) {
                        infoStop += ","
                    }
                    dataSource.deleteTrigger("${trigger.getId()}")
                    i++
                }
            }
            infoStop += "]"
            if (listStop != null && listStop.size > 0) {
                sendNotify(context, UtilJson.makeMapParam("start", "triggers", "started", infoStop))
            }
            var infoAdd = "["
            var i = 0
            while (listAdd != null && i < listAdd.size) {
                val trigger = listAdd[i]
                try {
                    TimeTrigger.updateTrigger(context, trigger)
                    infoAdd += "{\"id\":${trigger.getId()},\"state\":1}"
                    dataSource.createTrigger(trigger)
                } catch (te: TriggerException) {
                    infoAdd += "{\"id\":${trigger.getId()},\"state\":${te.code}}"
                }
                if (i + 1 < listAdd.size) {
                    infoAdd += ","
                }
                i++
            }
            infoAdd += "]"
            if (listAdd != null && listAdd.size > 0) {
                sendNotify(context, UtilJson.makeMapParam("start", "triggers", "started", infoAdd))
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    /**
     * Converts a list of TriggerDto objects to a map where the key is the trigger ID and the value is the TriggerDto object.
     *
     * @param list The list of TriggerDto objects to convert.
     * @return A map of trigger IDs to TriggerDto objects, or null if the input list is null.
     */
    private fun convertMap(list: List<TriggerDto>?): Map<Int, TriggerDto>? {
        if (list == null) {
            return null
        }
        val map: MutableMap<Int, TriggerDto> = HashMap()
        for (i in list.indices) {
            val tri = list[i]
            map[tri.getId()] = tri
        }
        return map
    }

    /**
     * Cancels any alarms associated with the given list of TriggerDto objects.
     *
     * @param context The application context.
     * @param list The list of TriggerDto objects for which to cancel alarms.
     */
    fun cancelAlarm(context: Context, list: List<TriggerDto>?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var i = 0
        while (list != null && i < list.size) {
            val trigger = list[i]
            val events = trigger.getEvents()
            if (events.indexOf(TimeTrigger.EXACT_TIME) > 0) {
                val intent = Intent(context, TimeTriggerReceiver::class.java)
                intent.putExtra("trigger_id", "${trigger.getId()}")
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (trigger.getId() * TimeTrigger.ADD_TRIGGER_ID),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
            if (events.indexOf(TimeTrigger.REPEAT_TIME) > 0) {
                val listEvents = TriggerParse.TriggerEvents(events)
                var j = 0
                while (listEvents != null && j < listEvents.size) {
                    val event = listEvents[j]
                    if ("repeat_time" == event.getType()) {
                        try {
                            val json = JSONObject(event.getInfo())
                            val array = json.getJSONArray("days_of_week")
                            var x = 0
                            while (array != null && x < array.length()) {
                                val day = array.getInt(x)
                                val intent = Intent(context, TimeTriggerReceiver::class.java)
                                intent.putExtra("trigger_id", "${trigger.getId()}")
                                val pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    (trigger.getId() * TimeTrigger.ADD_TRIGGER_ID + day),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                alarmManager.cancel(pendingIntent)
                                x++
                            }
                        } catch (e: Exception) {
                            PreyLogger.e("Error cancelAlarm:${e.message}", e)
                        }
                    }
                    j++
                }
            }
            i++
        }
    }

    /**
     * Sends a notification to the server with the given parameters.
     *
     * @param context The application context.
     * @param params The parameters to include in the notification.
     */
    fun sendNotify(context: Context, params: MutableMap<String, String?>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PreyConfig.getInstance(context).getWebServices()
                    .sendNotifyActionResultPreyHttp(context, params)
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
    }

    companion object {
        private var instance: TriggerController? = null
        fun getInstance(): TriggerController {
            return instance ?: TriggerController().also { instance = it }
        }
    }

}