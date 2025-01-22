/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.observer.kotlin

import android.content.Context
import com.prey.actions.kotlin.HttpDataService
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.util.kotlin.ClassUtil
import org.json.JSONException
import org.json.JSONObject

class ActionsController {

    fun runActionJson(ctx: Context, jsonObjectList: List<JSONObject>): List<HttpDataService>? {
        var listData: List<HttpDataService>? = ArrayList()
        val size = jsonObjectList?.size ?: -1
        PreyLogger.d(String.format("runActionJson size:%s", size))
        try {
            var i = 0
            while (jsonObjectList != null && i < jsonObjectList.size) {
                var jsonObject = jsonObjectList[i]
                try {
                    val jsonCmd = UtilJson.getJSONObject(jsonObject, "cmd")
                    if (jsonCmd != null) {
                        jsonObject = jsonCmd
                    }
                } catch (e: Exception) {
                    PreyLogger.e(String.format("Error:%s", e.message), e)
                }
                PreyLogger.d(String.format("jsonObject:%s", jsonObject))
                val nameAction = UtilJson.getString(jsonObject, "target")
                val methodAction = UtilJson.getString(jsonObject, "command")
                var parametersAction: JSONObject? = null
                try {
                    parametersAction = UtilJson.getJSONObject(jsonObject, "options")
                } catch (e: JSONException) {
                    PreyLogger.e(String.format("Error:%s", e.message), e)
                }
                if (parametersAction == null) {
                    parametersAction = JSONObject()
                }
                try {
                    val messageId = UtilJson.getString(jsonObject, PreyConfig.MESSAGE_ID)
                    if (messageId != null) {
                        parametersAction.put(PreyConfig.MESSAGE_ID, messageId)
                    }
                } catch (e: Exception) {
                    PreyLogger.e(String.format("Error:%s", e.message), e)
                }
                PreyLogger.d(
                    String.format(
                        "nameAction:%s methodAction:%s parametersAction:%s",
                        nameAction,
                        methodAction,
                        parametersAction
                    )
                )
                val listAction: List<ActionResult> = ArrayList()
                listData = ClassUtil.getInstance().execute(
                    ctx,
                    listAction,
                    nameAction!!,
                    methodAction!!,
                    parametersAction,
                    listData
                )

                i++
            }
            return listData
        } catch (e: JSONException) {
            PreyLogger.e("Error, causa:" + e.message, e)
        }
        return null
    }

    companion object {
        private var instance: ActionsController? = null
        fun getInstance(): ActionsController {
            if (ActionsController.instance == null) {
                ActionsController.instance = ActionsController()
            }
            return ActionsController.instance!!
        }
    }
}