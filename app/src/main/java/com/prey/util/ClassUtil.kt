/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util

import android.content.Context

import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.PreyLogger

import org.json.JSONObject

/**
 * Utility class for executing actions based on the provided parameters.
 */
class ClassUtil {

    /**
     * Executes an action based on the provided parameters and returns the resulting HTTP data services.
     *
     * @param context The application context.
     * @param listActions A list of action results.
     * @param nameAction The name of the action to be executed.
     * @param methodAction The method of the action to be executed.
     * @param parametersAction A JSON object containing parameters for the action.
     * @param listData A list of HTTP data services to be updated with the result.
     * @return The updated list of HTTP data services.
     */
    fun execute(
        context: Context,
        listActions: MutableList<ActionResult>?,
        nameAction: String,
        methodAction: String,
        parametersAction: JSONObject?,
        listData: MutableList<HttpDataService>?
    ): MutableList<HttpDataService>? {
        val nameActionClass = StringUtil().classFormat(nameAction)
        PreyLogger.d("name:$nameActionClass")
        PreyLogger.d("target:$methodAction")
        PreyLogger.d("options:$parametersAction")
        try {
            val actionClass = Class.forName("com.prey.json.actions.$nameActionClass")
            val actionObject = actionClass.newInstance()
            val method = actionClass.getMethod(
                methodAction, *arrayOf(
                    Context::class.java,
                    MutableList::class.java,
                    JSONObject::class.java
                )
            )
            val params = arrayOf(context, listActions, parametersAction)
            var listDataTmp: MutableList<HttpDataService>? = null
            try {
                listDataTmp = method.invoke(actionObject, *params) as ArrayList<HttpDataService>
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var i = 0
            while (listDataTmp != null && i < listDataTmp.size) {
                val httpDataService = listDataTmp[i]
                if (httpDataService != null) listData!!.add(httpDataService)
                i++
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return listData
    }

    companion object {
        private var instance: ClassUtil? = null
        fun getInstance(): ClassUtil {
            return instance ?: ClassUtil().also { instance = it }
        }
    }

}