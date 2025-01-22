/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert.kotlin

import android.content.Context

class AlertConfig(private val context: Context) {
    private var nextNotificationId = 100

    fun getNextNotificationId(): Int {
        return nextNotificationId++
    }

    companion object {
        private var instance: AlertConfig? = null

        @Synchronized
        fun getInstance(context: Context): AlertConfig {
            if (instance == null) {
                instance = AlertConfig(context)
            }
            return instance!!
        }
    }
}