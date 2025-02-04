/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.prey.actions.observer.ActionResult
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Tree {
    fun get(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject) {
        var messageId: String? = null
        try {
            messageId = UtilJson.getString(parameters, PreyConfig.MESSAGE_ID)
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        var reason: String? = null
        try {
            val jobId = UtilJson.getString(parameters, PreyConfig.JOB_ID)
            PreyLogger.d(String.format("jobId:%s", jobId))
            if (jobId != null && "" != jobId) {
                reason = "{\"device_job_id\":\"$jobId\"}"
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        try {
            PreyLogger.d("Tree started")
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                messageId,
                UtilJson.makeMapParam("get", "tree", "started", reason)
            )
            var depth = 1
            try {
                depth = parameters.getString("depth").toInt()
            } catch (e: Exception) {
            }
            var path = parameters.getString("path")
            if ("sdcard" == path) {
                path = "/"
            }
            val pathBase = Environment.getExternalStorageDirectory().toString()
            val dir = File(pathBase + path)
            val array = getFilesRecursiveJSON(pathBase, dir, depth - 1)
            val jsonTree = JSONObject()
            jsonTree.put("tree", array.toString())
            val response = PreyWebServices.getInstance().sendTree(ctx, jsonTree)
            PreyLogger.d(String.format("Tree stopped response:%d", response!!.getStatusCode()))
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("get", "tree", "stopped", reason)
            )
            PreyLogger.d("Tree stopped")
        } catch (e: Exception) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                messageId,
                UtilJson.makeMapParam("get", "tree", "failed", e.message)
            )
            PreyLogger.d("Tree failed:" + e.message)
        }
    }

    /**
     * Method to get a list of files
     *
     * @param pathBase
     * @param folder
     * @param depth
     * @return jsonArray
     */
    private fun getFilesRecursiveJSON(pathBase: String, folder: File?, depth: Int): JSONArray {
        var depth = depth
        depth = 0
        var length = 0
        var arrayFile: Array<File>? = null
        try {
            arrayFile = folder!!.listFiles()
            length = arrayFile.size
        } catch (e: Exception) {
        }
        val array = JSONArray()
        try {
            var i = 0
            while (folder != null && arrayFile != null && i < length) {
                val child = arrayFile[i]
                val parent = child.parent.replace(pathBase, "")
                val json = JSONObject()
                var size = 0
                try {
                    size = child.listFiles().size
                } catch (e: Exception) {
                }
                if (child.isDirectory && size > 0) {
                    json.put("name", child.name)
                    json.put("path", parent + "/" + child.name)
                    var listChildren = JSONArray()
                    if (depth > 0) {
                        listChildren = getFilesRecursiveJSON(pathBase, child, depth - 1)
                        json.put("children", listChildren)
                    }
                    json.put("isFile", false)
                    array.put(json)
                }
                if (child.isFile) {
                    val extension = MimeTypeMap.getFileExtensionFromUrl(child.name)
                    val mime = MimeTypeMap.getSingleton()
                    json.put("name", child.name)
                    json.put("path", parent + "/" + child.name)
                    json.put("mimetype", mime.getMimeTypeFromExtension(extension))
                    json.put("size", child.length())
                    json.put("isFile", true)
                    json.put("hidden", false)
                    array.put(json)
                }
                i++
            }
        } catch (e: Exception) {
            PreyLogger.e("Error getFilesRecursiveJSON:" + e.message, e)
        }
        return array
    }
}