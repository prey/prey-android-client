/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.Context
import android.content.Intent
import com.prey.PreyLogger
import org.json.JSONObject

/**
 * A BroadcastReceiver that handles geofence triggers.
 */
class TriggerGeoReceiver : TriggerReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
    }

    /**
     * Handles geofence trigger events.
     *
     * @param context The Context in which the receiver is running.
     * @param geoId The ID of the geofence that triggered the event.
     * @param transition The type of geofence transition (e.g., enter, exit).
     */
    fun onReceive(context: Context, geoId: Int, transition: String) {
        val dataSource = TriggerDataSource(context)
        val listTrigger = dataSource.allTriggers
        PreyLogger.d("Trigger onReceive geoId:$geoId transition:$transition")
        PreyLogger.d("Trigger onReceive listTrigger.size():${(listTrigger?.size ?: -1)}")
        var i = 0
        while (listTrigger != null && i < listTrigger.size) {
            val trigger = listTrigger[i]
            val triggerName = trigger.getName()
            PreyLogger.d("Trigger triggerName:$triggerName")
            val listEvents = TriggerParse.TriggerEvents(trigger.getEvents())
            var j = 0
            while (listEvents != null && j < listEvents.size) {
                val event = listEvents[j]
                if (transition == event.getType()) {
                    try {
                        val jsnobjectEvent = JSONObject(event.getInfo())
                        val eventGeoId = jsnobjectEvent.getInt("id")
                        PreyLogger.d("Trigger triggerName eventGeoId:$eventGeoId")
                        if (eventGeoId == geoId) {
                            var process = true
                            val haveRange = TriggerUtil.haveRange(listEvents)
                            if (haveRange) {
                                PreyLogger.d("Trigger TriggerReceiver  haveRange:$haveRange")
                                val validRange = TriggerUtil.validRange(listEvents)
                                PreyLogger.d("Trigger TriggerReceiver  validRange:$validRange")
                                process = validRange
                            }
                            if (process) {
                                PreyLogger.d("Trigger triggerName trigger.getActions():${trigger.getActions()}")
                                executeActions(context, trigger.getActions())
                            }
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                }
                j++
            }
            i++
        }
    }

}