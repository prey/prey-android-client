/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.json.kotlin.JsonAction
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPhone
import org.json.JSONObject

class PrivateIp : JsonAction() {
    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        val listResult = super.report(ctx, list, parameters)
        return listResult
    }

    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        PreyLogger.d("Ejecuting PrivateIp Data.")
        val listResult = super.get(ctx, list, parameters)
        return listResult
    }

    override fun run(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        val phone = PreyPhone(ctx)
        val data = HttpDataService("private_ip")
        val parametersMap = HashMap<String, String?>()
        val privateIp = phone.wifi!!.ipAddress!!
        parametersMap[privateIp] = privateIp
        PreyLogger.d("privateIp:$privateIp")
        data.setSingleData (privateIp)
        return data
    }
}