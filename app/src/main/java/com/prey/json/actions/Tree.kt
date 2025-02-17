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
import com.prey.net.PreyWebServices
import org.json.JSONArray
import org.json.JSONObject

import java.io.File

/**
 * Tree class responsible for handling file system operations.
 */
class Tree {

    /**
     * Retrieves a list of files and directories based on the provided parameters.
     *
     * @param context Context of the application.
     * @param actionResults List of ActionResult objects.
     * @param parameters JSONObject containing the parameters for the operation.
     */
    fun get(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject) {
        // Initialize variables to store message ID and reason.
        var reason: String? = null
        val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
        reason = try {
            val jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
            if (!jobId.isNullOrEmpty()) "{\"device_job_id\":\"$jobId\"}" else null
        } catch (e: Exception) {
            null
        }
        try {
            // Send notification to indicate the start of the operation.
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                messageId,
                UtilJson.makeMapParam("get", "tree", "started", reason)
            )
            val depth = parameters.optString("depth", "1").toInt()
            val path =
                if (parameters.optString("path") == "sdcard") "/" else parameters.optString("path")
            // Create a File object representing the directory.
            val directory = File("${Environment.getExternalStorageDirectory().toString()}$path")
            // Recursively retrieve a list of files and directories.
            val fileArray = getFilesRecursiveJson(directory.parent, directory, depth - 1)
            // Create a JSONObject to store the result.
            val treeJson = JSONObject()
            treeJson.put("tree", fileArray.toString())
            // Send the result to the server.
            val response = PreyWebServices.getInstance().sendTree(context, treeJson)
            // Send notification to indicate the end of the operation.
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("get", "tree", "stopped", reason)
            )
        } catch (e: Exception) {
            // If an exception occurs, send a notification with the error message.
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                messageId,
                UtilJson.makeMapParam("get", "tree", "failed", e.message)
            )
        }
    }

    /**
     * Recursively retrieves a list of files and directories.
     *
     * @param pathBase Base path of the directory.
     * @param folder File object representing the directory.
     * @param depth Maximum depth to recurse.
     * @return JSONArray containing the list of files and directories.
     */
    private fun getFilesRecursiveJson(pathBase: String, folder: File?, depth: Int): JSONArray {
        // Initialize an array to store the files.
        val files = folder?.listFiles() ?: emptyArray()
        // Initialize a JSONArray to store the result.
        val jsonArray = JSONArray()
        // Iterate over the files.
        files.forEach { file ->
            val parentPath = file.parent.replace(pathBase, "")
            val fileInfo = JSONObject()
            if (file.isDirectory && file.listFiles().isNotEmpty()) {
                // Add the directory information to the JSONObject.
                fileInfo.put("name", file.name)
                fileInfo.put("path", "$parentPath/${file.name}")
                // Recursively retrieve the contents of the directory.
                if (depth > 0) {
                    fileInfo.put("children", getFilesRecursiveJson(pathBase, file, depth - 1))
                }
                fileInfo.put("isFile", false)
            } else if (file.isFile) {
                // Get the file extension and MIME type.
                val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                // Add the file information to the JSONObject.
                fileInfo.put("name", file.name)
                fileInfo.put("path", "$parentPath/${file.name}")
                fileInfo.put("mimetype", mimeType)
                fileInfo.put("size", file.length())
                fileInfo.put("isFile", true)
                fileInfo.put("hidden", false)
            }
            jsonArray.put(fileInfo)
        }
        return jsonArray
    }
}