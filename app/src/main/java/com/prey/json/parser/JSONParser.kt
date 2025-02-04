/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.parser

import android.content.Context
import com.prey.PreyLogger
import com.prey.net.PreyRestHttpClient
import org.json.JSONException
import org.json.JSONObject

class JSONParser {
    var jObj: JSONObject? = null

    var error: Boolean = false

    fun getJSONFromUrl(ctx: Context, uri: String): List<JSONObject>? {
        PreyLogger.d("getJSONFromUrl:$uri")
        var sb: String? = null
        var json: String? = null
        try {
            val params = HashMap<String, String?>()
            val response = PreyRestHttpClient.getInstance(ctx).get(uri, params)
            try {
                sb = response!!.getResponseAsString()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            if (sb != null) json = sb.trim { it <= ' ' }
        } catch (e: Exception) {
            PreyLogger.e("Error, causa:" + e.message, e)
            return null
        }
        if (sb != null) {
            PreyLogger.d("_______cmd________")
            PreyLogger.d(sb)
        }
        //json = "[{\"command\":\"history\",\"target\":\"call\",\"options\":{}}]";
        //json = "[{\"command\":\"history\",\"target\":\"sms\",\"options\":{}}]";
        //json = "[{\"command\":\"history\",\"target\":\"contact\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"system_install\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"server\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"ring\",\"options\":{}}]";
        //json = "[{\"command\":\"start\",\"target\":\"video\",\"options\":{}}]";
        //json = "[{\"command\":\"get\",\"target\":\"picture\",\"options\":{}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\",\"picture\",\"location\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"screenshot\"]}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"location\"]}}]";
        //json = "[{\"command\":\"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.60713481,-36.42372147\",\"radius\": \"100\" }}]";
        //json = "[{\"command\":\"start\",\"target\": \"geofencing\",\"options\": {\"origin\": \"-70.7193117,-32.7521112\",\"radius\": \"100\" }}]";
        //json = "[{\"command\":\"start\",\"target\": \"geofencing\",\"options\": {\"id\":\"id1\",\"origin\":\"-70.60713481,-33.42372147\",\"radius\":\"100\",\"type:\":\"in",\"expire":"-1" }}]";
        //json = "[{\"command\":\"stop\",\"target\": \"geofencing\",\"options\": {\"id\":\"id1\"}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}},{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}, {\"command\":\"get\",\"target\":\"report\",\"options\":{\"delay\": \"25\",\"include\"[\"picture\",\"location\",\"screenshot\",\"access_points_list\"]}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}
        //json = "[{\"command\": \"get\",\"target\": \"location\",\"options\": {}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alert\",\"options\":{\"message\":\"This device i.\"}}]";
        //json = "[{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}]";
        //json = "[{\"command\":\"start\",\"target\":\"alarm\",\"options\":null}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"interval\":\"2\"}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"screenshot\",\"access_points_list\"],\"interval\":\"10\"}}]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"include\":[\"picture\",\"location\",\"access_points_list\"],\"interval\":\"10\"}}]";
        //json = "[{\"command\":\"start\",\"target\":\"camouflage\",\"options\":null}]";
        //json = "[{\"command\":\"stop\",\"target\":\"camouflage\",\"options\":{\"interval\":\"2\"}}}]";
        //json = "[{\"target\":\"alert\",\"command\":\"start\",\"options\":{\"alert_message\":\"This device is stolen property. Please contact testforkhq@gmail.com to arrange its safe return.\"}},{\"target\":\"lock\",\"command\":\"start\",\"options\":{\"unlock_pass\":\"oso\"}},{\"command\":\"get\",\"target\":\"location\"},{\"target\":\"network\",\"command\":\"start\"},{\"target\":\"geo\",\"command\":\"start\"}]";
        //json = "[{\"command\":\"start\",\"target\":\"contacts_backup\" }]";
        //json = "[{\"command\":\"start\",\"target\":\"contacts_restore\" }]";
        //json = "[{\"command\":\"start\",\"target\":\"browser\" }]";
        //json = "[{\"command\":\"get\",\"target\":\"report\",\"options\":{\"interval\":\"2\",\"exclude\":[\"picture\",false]}}]";
        //json = "[{\"command\":\"start\",\"target\": \"detach\",\"options\": {}}]";
        if ("[]" == json) {
            return null
        }
        if ("Invalid." == json) {
            return null
        }
        return getJSONFromTxt(ctx, json!!)
    }

    fun getJSONFromTxt(ctx: Context, json: String): List<JSONObject>? {
        var json = json

        if ("Invalid data received".equals(json)) return null
        if ("[null]".equals(json)) return null
        if ("".equals(json)) return null
        if (json == null) return null

        val listaJson: MutableList<JSONObject> = ArrayList()
        json = "{\"prey\":$json}"
        PreyLogger.d(json)
        try {
            val jsnobject = JSONObject(json)
            val jsonArray = jsnobject.getJSONArray("prey")
            for (i in 0 until jsonArray.length()) {
                val jsonCommand = jsonArray[i].toString()
                val explrObject = JSONObject(jsonCommand)
                PreyLogger.d(explrObject.toString())
                listaJson.add(explrObject)
            }
        } catch (e: Exception) {
            PreyLogger.e("error in parser:" + e.message, e)
        }
        return listaJson
    }

    fun getJSONFromTxt2(ctx: Context?, json: String): List<JSONObject> {
        jObj = null
        val listaJson: MutableList<JSONObject> = ArrayList()
        val listCommands = getListCommands(json)
        var i = 0
        while (listCommands != null && i < listCommands.size) {
            val command = listCommands[i]
            try {
                jObj = JSONObject(command)
                listaJson.add(jObj!!)
            } catch (e: JSONException) {
                PreyLogger.e("JSON Parser, Error parsing data $e", e)
            }
            i++
        }
        PreyLogger.d("json:$json")
        // return JSON String
        return listaJson
    }

    private fun getListCommands(json: String): List<String> {
        return if (json.indexOf("[{" + COMMAND) == 0) {
            getListCommandsCmd(json)
        } else {
            getListCommandsTarget(json)
        }
    }

    private fun getListCommandsTarget(json: String): List<String> {
        var json = json
        json = json.replace("nil".toRegex(), "{}")
        json = json.replace("null".toRegex(), "{}")
        val lista: MutableList<String> = ArrayList()
        var posicion = json.indexOf(TARGET)
        json = json.substring(posicion + 8)
        posicion = json.indexOf(TARGET)
        var command = ""
        while (posicion > 0) {
            command = json.substring(0, posicion)
            json = json.substring(posicion + 8)
            lista.add("{" + TARGET + cleanChar(command))
            posicion = json.indexOf("\"target\"")
        }
        lista.add("{" + TARGET + cleanChar(json))
        return lista
    }

    private fun getListCommandsCmd(json: String): List<String> {
        var json = json
        json = json.replace("nil".toRegex(), "{}")
        json = json.replace("null".toRegex(), "{}")
        val lista: MutableList<String> = ArrayList()
        var posicion = json.indexOf(COMMAND)
        json = json.substring(posicion + 9)
        posicion = json.indexOf(COMMAND)
        var command = ""
        while (posicion > 0) {
            command = json.substring(0, posicion)
            json = json.substring(posicion + 9)
            lista.add("{" + COMMAND + cleanChar(command))
            posicion = json.indexOf("\"command\"")
        }
        lista.add("{" + COMMAND + cleanChar(json))
        return lista
    }

    private fun cleanChar(json: String): String? {
        var json: String? = json
        if (json != null) {
            json = json.trim { it <= ' ' }
            var c = json[json.length - 1]
            while (c == '{' || c == ',' || c == ']') {
                json = json!!.substring(0, json.length - 1)
                json = json.trim { it <= ' ' }
                c = json[json.length - 1]
            }
        }
        return json
    }

    companion object {
        private const val COMMAND = "\"command\""
        private const val TARGET = "\"target\""
    }
}