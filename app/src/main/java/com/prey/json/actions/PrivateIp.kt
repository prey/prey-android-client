/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.json.JsonAction
import com.prey.PreyLogger
import com.prey.PreyPhone
import org.json.JSONObject

class PrivateIp : JsonAction() {
    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val listResult = super.report(ctx, list, parameters)
        return listResult
    }

    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        PreyLogger.d("Ejecuting PrivateIp Data.")
        val listResult = super.get(ctx, list, parameters)
        return listResult
    }

    override fun run(
        context: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService {
        val phone = PreyPhone.getInstance(context)
        val data = HttpDataService("private_ip")
        val parametersMap = HashMap<String, String?>()
        val privateIp = phone.getWifi()!!.getIpAddress()
        parametersMap[privateIp] = privateIp
        PreyLogger.d("privateIp:$privateIp")
        data.setSingleData (privateIp)
        return data
    }
}