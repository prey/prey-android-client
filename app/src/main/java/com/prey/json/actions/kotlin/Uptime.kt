/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import android.os.SystemClock
import com.prey.actions.kotlin.HttpDataService
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.json.kotlin.JsonAction
import org.json.JSONObject

class Uptime : JsonAction() {
    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): List<HttpDataService>? {
        val listResult = super.get(ctx, list, parameters)
        return listResult
    }

    override fun run(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        val uptime = SystemClock.uptimeMillis()
        val data = HttpDataService("uptime")
        val uptimeData = uptime.toString()
        data.setSingleData(uptimeData)
        return data
    }
}