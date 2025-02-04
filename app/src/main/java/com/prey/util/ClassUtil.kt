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

class ClassUtil {
    fun execute(
        ctx: Context,
        listActions: MutableList<ActionResult>,
        nameAction: String,
        methodAction: String,
        parametersAction: JSONObject?,
        listData: MutableList<HttpDataService>?
    ): MutableList<HttpDataService>? {
        var nameActionClass = nameAction
        PreyLogger.d("name:$nameActionClass")
        PreyLogger.d("target:$methodAction")
        PreyLogger.d("options:$parametersAction")
        nameActionClass = StringUtil.classFormat(nameActionClass)


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
            val params = arrayOf(ctx, listActions, parametersAction)
            var listDataTmp: MutableList<HttpDataService>? =null
            try{
                listDataTmp = method.invoke(actionObject, *params) as ArrayList<HttpDataService>
            } catch (e: Exception) {

            }

            var i = 0
            while (listDataTmp != null && i < listDataTmp.size) {
                val httpDataService = listDataTmp[i]
                if (httpDataService != null) listData!!.add(httpDataService)
                i++
            }
        } catch (e: Exception) {
            PreyLogger.e("Error, causa:" + e.message, e);
        }
        return listData
    }

    companion object {
        private var INSTANCE: ClassUtil? = null
        fun getInstance(): ClassUtil {
            if (INSTANCE == null) {
                INSTANCE = ClassUtil()
            }
            return INSTANCE!!
        }
    }
}