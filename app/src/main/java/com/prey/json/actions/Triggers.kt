package com.prey.json.actions

import android.content.Context
import com.prey.PreyLogger
import com.prey.actions.observer.ActionResult
import com.prey.actions.triggers.TriggerController
import org.json.JSONObject

class Triggers {

    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        PreyLogger.d("starting Triggers")
        try {
            //Wait before executing.
            Thread.sleep(2000)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        TriggerController.getInstance().run(context)
    }

    fun stop(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        PreyLogger.d("stop Triggers")
        TriggerController.getInstance().run(context)
    }

}