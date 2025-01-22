/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers.kotlin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import com.prey.net.kotlin.UtilConnection

import org.json.JSONObject

class TriggerController {
    private var listBD: List<TriggerDto>? = null
    private var listWeb: List<TriggerDto>? = null

    fun run(ctx: Context) {
        try {
            if (UtilConnection.getInstance().isInternetAvailable()) {
                Thread.sleep(1000)
                val dataSource = TriggerDataSource(ctx)
                listBD = dataSource.allTriggers
                listWeb = null
                try {
                    listWeb = TriggerParse.getJSONFromUrl(ctx)
                } catch (e: Exception) {
                    PreyLogger.e("error TriggerController get json:" + e.message, e)
                }
                updateTriggers(ctx, listWeb, listBD, dataSource)
            }
        } catch (e: Exception) {
            PreyLogger.e("error TriggerController run:" + e.message, e)
        }
    }

    private fun updateTriggers(
        ctx: Context,
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
                        dataSource.deleteTrigger("" + dto.getId())
                    }
                    i++
                }
            }
            if (listDel.size > 0) {
                cancelAlarm(ctx, listDel)
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
                sendNotify(ctx, UtilJson.makeMapParam("start", "triggers", "stopped", infoDelete))
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
                        TimeTrigger.updateTrigger(ctx, trigger)
                    } catch (te: TriggerException) {
                        listStop.add(trigger)
                        PreyLogger.d("TimeTrigger listRun exception id:" + trigger.getId() + " ,state:" + te.code)
                    }
                    i++
                }
            }
            var infoStop = "["
            run {
                var i = 0
                while (listStop != null && i < listStop.size) {
                    val trigger = listStop[i]
                    infoStop += "{\"id\":" + trigger.getId() + ",\"state\":3}"
                    if (i + 1 < listStop.size) {
                        infoStop += ","
                    }
                    dataSource.deleteTrigger("" + trigger.getId())
                    i++
                }
            }
            infoStop += "]"
            if (listStop != null && listStop.size > 0) {
                sendNotify(ctx, UtilJson.makeMapParam("start", "triggers", "started", infoStop))
            }
            var infoAdd = "["
            var i = 0
            while (listAdd != null && i < listAdd.size) {
                val trigger = listAdd[i]
                try {
                    TimeTrigger.updateTrigger(ctx, trigger)
                    infoAdd += "{\"id\":" + trigger.getId() + ",\"state\":1}"
                    dataSource.createTrigger(trigger)
                } catch (te: TriggerException) {
                    infoAdd += "{\"id\":" + trigger.getId() + ",\"state\":" + te.code + "}"
                }
                if (i + 1 < listAdd.size) {
                    infoAdd += ","
                }
                i++
            }
            infoAdd += "]"
            if (listAdd != null && listAdd.size > 0) {
                sendNotify(ctx, UtilJson.makeMapParam("start", "triggers", "started", infoAdd))
            }
        } catch (e: Exception) {
            PreyLogger.e("error run" + e.message, e)
        }
    }

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

    fun cancelAlarm(ctx: Context, list: List<TriggerDto>?) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var i = 0
        while (list != null && i < list.size) {
            val trigger = list[i]
            val events = trigger.getEvents()
            if (events.indexOf(TimeTrigger.EXACT_TIME) > 0) {
                val intent = Intent(ctx, TimeTriggerReceiver::class.java)
                intent.putExtra("trigger_id", "" + trigger.getId())
                val pendingIntent = PendingIntent.getBroadcast(
                    ctx,
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
                                val intent = Intent(ctx, TimeTriggerReceiver::class.java)
                                intent.putExtra("trigger_id", "" + trigger.getId())
                                val pendingIntent = PendingIntent.getBroadcast(
                                    ctx,
                                    (trigger.getId() * TimeTrigger.ADD_TRIGGER_ID + day),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                alarmManager.cancel(pendingIntent)
                                x++
                            }
                        } catch (e: Exception) {
                            PreyLogger.e("Error cancelAlarm:" + e.message, e)
                        }
                    }
                    j++
                }
            }
            i++
        }
    }

    fun sendNotify(ctx: Context, params: MutableMap<String, String?>) {
        object : Thread() {
            override fun run() {
                try {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, params)
                } catch (e: Exception) {
                }
            }
        }.start()
    }

    companion object {
        private var instance: TriggerController? = null
        fun getInstance(): TriggerController {
            if (instance == null) {
                instance = TriggerController()
            }
            return instance!!
        }
    }
}