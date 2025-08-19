/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.parser

import com.prey.PreyLogger
import com.prey.net.PreyRestHttpClient

import org.json.JSONObject

/**
 * A utility class for parsing JSON data.
 */
class JSONParser {
    var jsonObject: JSONObject? = null
    var error: Boolean = false

    /**
     * Retrieves JSON data from a URL and returns a list of JSON objects.
     *
     * @param context The application context.
     * @param uri The URL to retrieve JSON data from.
     * @return A list of JSON objects, or null if an error occurred.
     */
    fun getJSONFromUrl(preyRestHttpClient: PreyRestHttpClient, uri: String): List<JSONObject>? {
        PreyLogger.d("getJSONFromUrl:$uri")
        var sb: String? = null
        var json: String? = null
        try {
            val params = HashMap<String, String?>()
            val response = preyRestHttpClient.get(uri, params)
            try {
                if (response != null) {
                    sb = response.getResponseAsString()
                }
            } catch (e: Exception) {
                PreyLogger.e("Error: ${e.message}", e)
            }
            if (sb != null) json = sb.trim { it <= ' ' }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
            return null
        }
        if (sb != null) {
            PreyLogger.d("_______cmd________")
            PreyLogger.d(sb)
        }
        if ("[]" == json) {
            return null
        }
        if ("Invalid." == json) {
            return null
        }
        return getJSONFromTxt(json!!)
    }

    /**
     * Parses a JSON string and returns a list of JSON objects.
     *
     * @param context The application context.
     * @param jsonString The JSON string to parse.
     * @return A list of JSON objects, or null if an error occurred.
     */
    fun getJSONFromTxt(jsonString: String): List<JSONObject>? {
        if ("Invalid JSON".equals(jsonString)) return null
        if ("Invalid data received".equals(jsonString)) return null
        if ("[null]".equals(jsonString)) return null
        if ("".equals(jsonString)) return null
        val jsonList: MutableList<JSONObject> = ArrayList()
        PreyLogger.d("jsonString:${jsonString}")
        try {
            val jsonObject = JSONObject("{\"prey\":$jsonString}")
            val jsonArray = jsonObject.getJSONArray("prey")
            for (i in 0 until jsonArray.length()) {
                val jsonCommand = jsonArray[i].toString()
                val commandObject = JSONObject(jsonCommand)
                PreyLogger.d(commandObject.toString())
                jsonList.add(commandObject)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        return jsonList
    }

    /**
     * Retrieves a list of commands from a JSON string.
     *
     * @param json The JSON string to parse.
     * @return A list of commands, or an empty list if the input string is invalid.
     */
    fun getListCommands(json: String): List<String> {
        return if (json.indexOf("[{${COMMAND}") == 0) {
            extractCommandsFromCommandJson(json)
        } else {
            extractCommandsFromTargetJson(json)
        }
    }

    /**
     * Retrieves a list of commands from a JSON string that starts with a target array.
     *
     * @param json The JSON string to parse.
     * @return A list of commands.
     */
    fun extractCommandsFromTargetJson(json: String): List<String> {
        var jsonString = json.replace("nil".toRegex(), "{}")
        jsonString = jsonString.replace("null".toRegex(), "{}")
        val commands: MutableList<String> = ArrayList()
        var position = jsonString.indexOf(TARGET)
        jsonString = jsonString.substring(position + 8)
        position = jsonString.indexOf(TARGET)
        var command = ""
        while (position > 0) {
            command = jsonString.substring(0, position)
            jsonString = jsonString.substring(position + 8)
            commands.add("{${TARGET}${cleanChar(command)}")
            position = jsonString.indexOf("\"target\"")
        }
        commands.add("{${TARGET}${cleanChar(jsonString)}")
        return commands
    }

    /**
     * Retrieves a list of commands from a JSON string that starts with a command array.
     *
     * @param json The JSON string to parse.
     * @return A list of commands.
     */
    fun extractCommandsFromCommandJson(json: String): List<String> {
        var jsonString = json.replace("nil".toRegex(), "{}")
        jsonString = jsonString.replace("null".toRegex(), "{}")
        val jsonList: MutableList<String> = ArrayList()
        var position = jsonString.indexOf(COMMAND)
        jsonString = jsonString.substring(position + 9)
        position = jsonString.indexOf(COMMAND)
        var command = ""
        while (position > 0) {
            command = jsonString.substring(0, position)
            jsonString = jsonString.substring(position + 9)
            jsonList.add("{${COMMAND}${cleanChar(command)}")
            position = jsonString.indexOf("\"command\"")
        }
        jsonList.add("{${COMMAND}${cleanChar(jsonString)}")
        return jsonList
    }

    /**
     * Cleans a JSON string by removing trailing characters.
     *
     * @param json The JSON string to clean.
     * @return The cleaned JSON string.
     */
    fun cleanChar(json: String): String? {
        var jsonString: String? = json
        if (jsonString != null) {
            jsonString = jsonString.trim { it <= ' ' }
            var c = jsonString[jsonString.length - 1]
            while (c == '{' || c == ',' || c == ']') {
                jsonString = jsonString!!.substring(0, jsonString.length - 1)
                jsonString = jsonString.trim { it <= ' ' }
                c = jsonString[jsonString.length - 1]
            }
        }
        return jsonString
    }

    companion object {
        private const val COMMAND = "\"command\""
        private const val TARGET = "\"target\""
    }

}