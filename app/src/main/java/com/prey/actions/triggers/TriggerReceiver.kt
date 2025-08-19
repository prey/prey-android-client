/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.prey.actions.observer.ActionResult
import com.prey.PreyLogger
import com.prey.util.ClassUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.json.JSONException
import org.json.JSONObject

/**
 * Abstract base class for trigger receivers.
 * Provides a basic implementation for executing triggers and actions.
 */
abstract class TriggerReceiver : BroadcastReceiver() {
    abstract override fun onReceive(context: Context, intent: Intent)

    /**
     * Executes a trigger with the given name.
     * Retrieves all triggers from the data source, checks if the trigger has a matching event,
     * and executes the corresponding actions.
     *
     * @param context The Context in which the receiver is running.
     * @param name The name of the trigger to execute.
     */
    fun execute(context: Context, name: String) {
        val dataSource = TriggerDataSource(context)
        val listTrigger = dataSource.allTriggers
        PreyLogger.d("Trigger TriggerReceiver onReceive name:$name")
        PreyLogger.d(
            "Trigger TriggerReceiver onReceive listTrigger.size():${
                (listTrigger.size
                    ?: -1)
            }"
        )
        var i = 0
        while (listTrigger != null && i < listTrigger.size) {
            val trigger = listTrigger[i]
            val listEvents = TriggerParse.TriggerEvents(trigger.getEvents())
            var j = 0
            while (listEvents != null && j < listEvents.size) {
                val event = listEvents[j]
                PreyLogger.d("Trigger TriggerReceiver onReceive name:${name} event.type:${event.getType()}")
                if (name == event.getType()) {
                    var process = true
                    val haveRange = TriggerUtil.haveRange(listEvents)
                    PreyLogger.d("Trigger TriggerReceiver  haveRange:$haveRange")
                    if (haveRange) {
                        val validRange = TriggerUtil.validRange(listEvents)
                        PreyLogger.d("Trigger TriggerReceiver  validRange:$validRange")
                        process = validRange
                    }
                    try {
                        if (process) {
                            PreyLogger.d("Trigger TriggerReceiver triggerName trigger.getActions():${trigger.getActions()}")
                            executeActions(context, trigger.getActions())
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

    /**
     * Executes a list of actions.
     * Parses the actions from the given string, executes each action with the given delay,
     * and logs any errors that occur.
     *
     * @param context The Context in which the receiver is running.
     * @param actions The string containing the actions to execute.
     * @throws Exception If an error occurs while executing the actions.
     */
    @Throws(Exception::class)
    fun executeActions(context: Context, actions: String) {
        val listActions = TriggerParse.TriggerActions(actions)
        var z = 0
        while (listActions != null && z < listActions.size) {
            val actionDto = listActions[z]
            val delay = actionDto.getDelay()
            PreyLogger.d("triggerName TriggerReceiver delay:$delay")
            if (delay > 0) {
                Thread.sleep((delay * 1000).toLong())
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    PreyLogger.d("Trigger triggerName actionDto.action:${actionDto.getAction()}")
                    val jsonObject = JSONObject(actionDto.getAction())
                    PreyLogger.d("Trigger triggerName action:$jsonObject")
                    PreyLogger.d("Trigger triggerName jsonObject:$jsonObject")
                    val nameAction = jsonObject.getString("target")
                    PreyLogger.d("Trigger triggerName nameAction:$nameAction")
                    val methodAction = jsonObject.getString("command")
                    PreyLogger.d("Trigger triggerName methodAction:$methodAction")
                    var parametersAction: JSONObject? = null
                    try {
                        parametersAction = jsonObject.getJSONObject("options")
                        PreyLogger.d("Trigger triggerName parametersAction:$parametersAction")
                    } catch (e: JSONException) {
                        PreyLogger.e("Error:${e.message}", e)
                    }
                    if (parametersAction == null) {
                        parametersAction = JSONObject()
                    }
                    PreyLogger.d("Trigger nameAction:$nameAction methodAction:$methodAction parametersAction:$parametersAction")
                    val listActions: MutableList<ActionResult> = ArrayList()
                    ClassUtil.getInstance().execute(
                        context,
                        listActions,
                        nameAction,
                        methodAction,
                        parametersAction,
                        null
                    )
                } catch (e: Exception) {
                    PreyLogger.e("Trigger error:${e.message}", e)
                }
            }
            z++
        }
    }

}