/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.kotlin

import android.content.Context
import com.prey.net.kotlin.PreyWebServices

class PreyStatus private constructor() {

    @JvmName("functionOfKotlin")
    fun isPreyConfigurationActivityResume(): Boolean {
        return  isPreyConfigurationActivityResume
    }
    @JvmName("functionOfKotlin")
    fun setPreyConfigurationActivityResume(
        preyConfigurationActivityResume: Boolean
    ) {
        isPreyConfigurationActivityResume= preyConfigurationActivityResume
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
                PreyConfig.getInstance(context).setAutoConnect ( autoconnect)
                PreyLogger.d(String.format("STATUS aware :%b", aware))
                PreyLogger.d(String.format("STATUS autoconnect :%b", autoconnect))
                minutesToQueryServer = try {
                    jsnobject.getInt("minutes_to_query_server")
                } catch (e: Exception) {
                    PreyConfig.getInstance(context).getMinutesToQueryServer()
                }
                PreyConfig.getInstance(context).setMinutesToQueryServer(minutesToQueryServer)
                PreyLogger.d(
                    String.format(
                        "STATUS minutesToQueryServer :%s",
                        minutesToQueryServer
                    )
                )
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("STATUS Error:%s", e.message), e)
        }
    }

    companion object {
        @Volatile
        private var instance: PreyStatus? = null

        fun getInstance(): PreyStatus {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = PreyStatus()
                    }
                }
            }
            return instance!!
        }

    }
}