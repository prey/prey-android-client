/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServices
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Represents a command target for file system tree operations.
 *
 * This class handles commands to retrieve the directory structure of the device's external storage.
 * It's designed to be invoked through the `CommandTarget` interface, processing JSON-based commands.
 * The primary operation is "get", which traverses a specified path on the external storage
 * up to a certain depth, collects file and directory information, and sends it to a web service.
 *
 * The process is executed asynchronously in a coroutine. It notifies the server about the
 * "started", "stopped", and "failed" states of the operation.
 */
class Tree : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    companion object {
        const val TARGET = "tree"
        const val PATH = "path"
        const val DEPTH ="depth"
    }

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_GET -> scope.launch { get(context, options) }
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Asynchronously retrieves the file system tree from the device's external storage and sends it to a web service.
     *
     * This function is launched in a background coroutine (IO dispatcher). It first notifies the
     * Prey web service that the "tree" retrieval process has started. It then proceeds to
     * recursively scan the file system starting from a specified path up to a given depth,
     * gathering information about files and directories.
     *
     * The collected file system data is formatted into a JSON object and sent to the Prey server.
     * After the data is sent, another notification is sent to indicate that the process has stopped.
     * If any error occurs during the process, a "failed" notification is sent with the error details.
     *
     * The operation's parameters are extracted from the `options` JSONObject:
     * - `path`: The starting directory path for the scan (e.g., "/"). The special value "sdcard" is treated as the root "/".
     * - `depth`: The maximum depth of the directory traversal. Defaults to 1 if not specified or invalid.
     * - `job_id`: A unique identifier for this job, used in notifications.
     * - `message_id`: A unique identifier for the message, used in notifications.
     *
     * @param context The application context, used for accessing system services like `PreyWebServices`.
     * @param options A JSONObject containing the parameters for the file tree retrieval,
     *                such as `path`, `depth`, and `job_id`.
     */
    suspend fun get(context: Context, options: JSONObject) {
        PreyLogger.d("Tree get options:${options}")
        val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
        val jobId = options.optString(PreyConfig.JOB_ID, null)
        val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
        try {
            PreyLogger.d("Tree started")
            PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_STARTED, reason)
            val depth = if (options.has(DEPTH)) options.getInt(DEPTH) else 1
            var path: String = options.getString(PATH)
            if ("sdcard" == path) {
                path = "/"
            }
            val pathBase = Environment.getExternalStorageDirectory().toString()
            val dir = File("${pathBase}${path}")
            val array: JSONArray = getFilesRecursiveJSON(pathBase, dir, depth - 1)
            val jsonTree = JSONObject()
            jsonTree.put("tree", array.toString())
            val response = PreyWebServices.getInstance().sendTree(context, jsonTree)
            PreyLogger.d("Tree stopped response:${response.statusCode}")
            PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_STOPPED, reason, messageId)
            PreyLogger.d("Tree stopped")
        } catch (e: Exception) {
            PreyWebServicesKt.notify(context, CMD_GET, TARGET, STATUS_FAILED, e.message)
            PreyLogger.e("Tree failed:${e.message}", e)
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
    fun getFilesRecursiveJSON(pathBase: String, folder: File?, depth: Int): JSONArray {
        var depth = depth
        depth = 0
        var length = 0
        var arrayFile: Array<File>? = null
        try {
            arrayFile = folder!!.listFiles()
            length = arrayFile.size
        } catch (e: java.lang.Exception) {
        }
        val array = JSONArray()
        try {
            var i = 0
            while (folder != null && arrayFile != null && i < length) {
                val child = arrayFile[i]
                val parent = child.getParent().replace(pathBase, "")
                val json = JSONObject()
                var size = 0
                try {
                    size = child.listFiles().size
                } catch (e: java.lang.Exception) {
                }
                if (child.isDirectory() && size > 0) {
                    json.put("name", child.getName())
                    json.put("path", "${parent}/${child.getName()}")
                    var listChildren = JSONArray()
                    if (depth > 0) {
                        listChildren = getFilesRecursiveJSON(pathBase, child, depth - 1)
                        json.put("children", listChildren)
                    }
                    json.put("isFile", false)
                    array.put(json)
                }
                if (child.isFile()) {
                    val extension = MimeTypeMap.getFileExtensionFromUrl(child.getName())
                    val mime = MimeTypeMap.getSingleton()
                    json.put("name", child.getName())
                    json.put("path", "${parent}/${child.getName()}")
                    json.put("mimetype", mime.getMimeTypeFromExtension(extension))
                    json.put("size", child.length())
                    json.put("isFile", true)
                    json.put("hidden", false)
                    array.put(json)
                } else {
                    val numberOfFiles: Int = numberOfFilesInTheFolder(child)
                    json.put("isEmpty", numberOfFiles == 0)
                }
                i++
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error getFilesRecursiveJSON:${e.message}", e)
        }
        return array
    }

    /**
     * Recursively counts files in a directory, excluding those with "trashed" in their names.
     *
     * @param folder The directory to count files in. Can be a file or directory.
     * @return count of non-trash files in the directory and its subdirectories
     */
    private fun numberOfFilesInTheFolder(folder: File?): Int {
        if (folder == null) {
            return 0
        }
        // If it's a file, check if it's not trashed
        if (folder.isFile()) {
            if (!folder.getName().contains("trashed")) {
                return 1
            }
            return 0
        }
        // If it's a directory, process its contents
        val files = folder.listFiles()
        if (files == null) {
            return 0 // In case of I/O error, listFiles() returns null
        }
        var count = 0
        for (file in files) {
            if (file.isDirectory()) {
                count += numberOfFilesInTheFolder(file)
            } else if (!file.getName().contains("trashed")) {
                count++
                //PreyLogger.d("Tree count:$count path:${file.getPath()}")
            }
        }
        return count
    }

}