/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util.kotlin

import android.content.Context
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.kotlin.PreyLogger
import com.prey.util.StringUtil
import org.json.JSONObject

class ClassUtil {
    fun execute(
        ctx: Context,
        list: List<ActionResult>,
        nameAction: String,
        methodAction: String,
        parametersAction: JSONObject?,
        listData: List<HttpDataService>?
    ): List<HttpDataService>? {
        var nameAction = nameAction
        PreyLogger.d("name:$nameAction")
        PreyLogger.d("target:$methodAction")
        PreyLogger.d("options:$parametersAction")
        nameAction = StringUtil.classFormat(nameAction)


        try {
            val actionClass = Class.forName("com.prey.json.actions.kotlin.$nameAction")
            val actionObject = actionClass.newInstance()
            val method = actionClass.getMethod(
                methodAction, *arrayOf(
                    Context::class.java,
                    MutableList::class.java,
                    JSONObject::class.java
                )
            )
            val params = arrayOf(ctx, list, parametersAction)
            val listDataTmp: List<HttpDataService> =
                method.invoke(actionObject, *params) as ArrayList<HttpDataService>
            var i = 0
            while (listDataTmp != null && i < listDataTmp.size) {
                val httpDataService = listDataTmp[i]
                if (httpDataService != null) listData!!.plus(httpDataService)
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
            if (ClassUtil.INSTANCE == null) {
                ClassUtil.INSTANCE = ClassUtil()
            }
            return ClassUtil.INSTANCE!!
        }
    }
}