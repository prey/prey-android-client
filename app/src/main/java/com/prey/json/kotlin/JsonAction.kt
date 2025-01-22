/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.kotlin

import android.content.Context
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import org.json.JSONObject

abstract class JsonAction {
    open fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d(javaClass.name)
        val dataToBeSent: MutableList<HttpDataService> = ArrayList()
        try {
            val data = run(ctx, list, parameters)
            val result = ActionResult()
            result.dataToSend = data
            list!!.plus(result)
            dataToBeSent.add(data!!)
        } catch (e: Exception) {
            PreyLogger.e("Error causa:" + e.message + e.message, e)
        }
        return dataToBeSent
    }

    open fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d(javaClass.name)
        val data = run(ctx, list, parameters)
        val dataToBeSent = ArrayList<HttpDataService>()
        dataToBeSent.add(data!!)
        PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent)
        return dataToBeSent
    }

    abstract fun run(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService?
}