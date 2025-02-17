/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.content.Context

import com.prey.net.PreyWebServices

class PreyStatus {

    @JvmName("functionOfKotlin")
    fun isPreyConfigurationActivityResume(): Boolean {
        return isPreyConfigurationActivityResume
    }

    @JvmName("functionOfKotlin")
    fun setPreyConfigurationActivityResume(
        preyConfigurationActivityResume: Boolean
    ) {
        isPreyConfigurationActivityResume = preyConfigurationActivityResume
    }

    var isPreyConfigurationActivityResume: Boolean = false

    var isPreyPopUpOnclick: Boolean = false

    var isTakenPicture: Boolean = false

    var isAlarmStart: Boolean = false
        private set

    fun setAlarmStart() {
        this.isAlarmStart = true
    }

    fun setAlarmStop() {
        this.isAlarmStart = false
    }

    /**
     * Method initialize device state
     * @param context
     */
    fun initConfig(context: Context) {
        var aware = false
        var autoconnect = false
        var minutesToQueryServer: Int
        try {
            val jsnobject = PreyWebServices.getInstance().getStatus(context)
            if (jsnobject != null) {
                PreyLogger.d("STATUS jsnobject :$jsnobject")
                val jsnobjectSettings = jsnobject.getJSONObject("settings")
                try {
                    val jsnobjectLocal = jsnobjectSettings.getJSONObject("local")
                    aware = jsnobjectLocal.getBoolean("location_aware")
                } catch (e: Exception) {
                    aware = false
                }
                try {
                    val jsnobjectGlobal = jsnobjectSettings.getJSONObject("global")
                    autoconnect = jsnobjectGlobal.getBoolean("auto_connect")
                } catch (e: Exception) {
                    autoconnect = false
                }
                PreyConfig.getInstance(context!!).setAware(aware)
                PreyConfig.getInstance(context).setAutoConnect(autoconnect)
                PreyLogger.d("STATUS aware :${aware}")
                PreyLogger.d("STATUS autoconnect :${autoconnect}")
                minutesToQueryServer = try {
                    jsnobject.getInt("minutes_to_query_server")
                } catch (e: Exception) {
                    PreyConfig.getInstance(context).getMinutesToQueryServer()
                }
                PreyConfig.getInstance(context).setMinutesToQueryServer(minutesToQueryServer)
                PreyLogger.d(

                    "STATUS minutesToQueryServer :${minutesToQueryServer}"
                )
            }
        } catch (e: Exception) {
            PreyLogger.e("STATUS Error:${e.message}", e)
        }
    }

    companion object {
        private var instance: PreyStatus? = null
        fun getInstance(): PreyStatus {
            return instance ?: PreyStatus().also { instance = it }
        }
    }
}