package com.prey.json.actions

import android.content.Context

import com.prey.actions.HttpDataService
import com.prey.actions.observer.ActionResult
import com.prey.actions.picture.PictureUtil
import com.prey.json.JsonAction

import org.json.JSONObject

/**
 * Represents a Picture action that extends JsonAction.
 */
class Picture : JsonAction() {

    /**
     * Reports the picture action.
     *
     * @param context The application context.
     * @param list A list of action results.
     * @param parameters A JSON object containing action parameters.
     * @return A list of HttpDataService objects.
     */
    override fun report(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val result = super.report(context, actionResults, parameters)
        return result
    }

    /**
     * Gets the picture action.
     *
     * @param context The application context.
     * @param list A list of action results.
     * @param parameters A JSON object containing action parameters.
     * @return A list of HttpDataService objects.
     */
    override fun get(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): MutableList<HttpDataService>? {
        val result = super.get(context, actionResults, parameters)
        return result
    }

    /**
     * Starts the picture action.
     *
     * @param context The application context.
     * @param list A list of action results.
     * @param parameters A JSON object containing action parameters.
     * @return An HttpDataService object.
     */
    fun start(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? = PictureUtil.getInstance(context).getPicture()

    /**
     * Runs the picture action.
     *
     * @param context The application context.
     * @param list A list of action results.
     * @param parameters A JSON object containing action parameters.
     * @return An HttpDataService object.
     */
    override fun run(
        context: Context,
        actionResults: MutableList<ActionResult>?,
        parameters: JSONObject?
    ): HttpDataService? = PictureUtil.getInstance(context).getPicture()

}