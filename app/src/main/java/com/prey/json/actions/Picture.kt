package com.prey.json.actions

import android.content.Context
import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.actions.picture.PictureUtil
import com.prey.json.JsonAction
import org.json.JSONObject

class Picture : JsonAction() {

    override fun report(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val listResult: MutableList<HttpDataService>? = super.report(ctx, list, parameters)
        return listResult
    }

    override fun get(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val listResult = super.get(ctx, list, parameters)
        return listResult
    }

    fun start(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        return PictureUtil.getInstance(ctx).getPicture(ctx);
    }

    override fun run(
        ctx: Context,
        list: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? {
        return PictureUtil.getInstance(ctx).getPicture(ctx);
    }
}