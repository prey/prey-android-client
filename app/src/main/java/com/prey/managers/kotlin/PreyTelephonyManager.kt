/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers.kotlin

import android.content.Context
import android.telephony.TelephonyManager
import com.prey.kotlin.PreyPhone

class PreyTelephonyManager {
    fun isDataConnectivityEnabled(ctx: Context): Boolean {
        return PreyPhone(ctx).dataState == TelephonyManager.DATA_CONNECTED
    }

    companion object {
        private var INSTANCE: PreyTelephonyManager? = null
        fun getInstance(): PreyTelephonyManager {
            if (PreyTelephonyManager.INSTANCE == null) {
                PreyTelephonyManager.INSTANCE = PreyTelephonyManager()
            }
            return PreyTelephonyManager.INSTANCE!!
        }
    }
}