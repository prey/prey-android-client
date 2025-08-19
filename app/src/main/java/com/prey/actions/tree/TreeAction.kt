package com.prey.actions.tree

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.prey.PreyConfig
import com.prey.json.UtilJson

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TreeAction {

    fun start(
        context: Context,
        messageId: String?,
        jobId: String?,
        path: String?,
        depth: Int
    ) {
        try {
            var reason: String? = null
            if (jobId != null && "" != jobId) {
                reason = "{\"device_job_id\":\"$jobId\"}"
            }
            // Send notification to indicate the start of the operation.
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                messageId,
                UtilJson.makeMapParam("get", "tree", "started", reason)
            )
            val pathBase = Environment.getExternalStorageDirectory().toString()
            val filePath = "${pathBase}${path}"
            // Create a File object representing the directory.
            val directory = File(filePath)
            if (!directory.exists()) {
                throw Exception("directory not exists $filePath")
            }
            // Recursively retrieve a list of files and directories.
            val fileArray = getFilesRecursiveJson(pathBase, directory, depth - 1)
            // Create a JSONObject to store the result.
            val treeJson = JSONObject()
            treeJson.put("tree", fileArray.toString())
            // Send the result to the server.
            val response =
                PreyConfig.getInstance(context).getWebServices().sendTree(context, treeJson)
            // Send notification to indicate the end of the operation.
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("get", "tree", "stopped", reason)
            )
        } catch (e: Exception) {
            // If an exception occurs, send a notification with the error message.
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
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
            var fileInfo: JSONObject? = null
            if (file.isDirectory && file.listFiles().isNotEmpty()) {
                fileInfo = JSONObject()
                // Add the directory information to the JSONObject.
                fileInfo.put("name", file.getName());
                fileInfo.put("path", "${parentPath}/${file.getName()}");
                // Recursively retrieve the contents of the directory.
                if (depth > 0) {
                    fileInfo.put("children", getFilesRecursiveJson(pathBase, file, depth - 1))
                }
                fileInfo.put("isFile", false)
            } else if (file.isFile) {
                fileInfo = JSONObject()
                // Get the file extension and MIME type.
                val extension = MimeTypeMap.getFileExtensionFromUrl(file.getName())
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                // Add the file information to the JSONObject.
                fileInfo.put("name", file.getName())
                fileInfo.put("path", "${parentPath}/${file.getName()}");
                fileInfo.put("mimetype", mimeType)
                fileInfo.put("size", file.length())
                fileInfo.put("isFile", true)
                fileInfo.put("hidden", false)
            }
            if (fileInfo != null) {
                jsonArray.put(fileInfo)
            }
        }
        return jsonArray
    }

}