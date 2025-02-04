/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.managers

import android.content.Context
import android.telephony.TelephonyManager
import com.prey.PreyPhone

class PreyTelephonyManager {
    fun isDataConnectivityEnabled(ctx: Context): Boolean {
        return PreyPhone.getInstance(ctx).dataState == TelephonyManager.DATA_CONNECTED
    }

    companion object {
        private var INSTANCE: PreyTelephonyManager? = null
        fun getInstance(): PreyTelephonyManager {
            if (INSTANCE == null) {
                INSTANCE = PreyTelephonyManager()
            }
            return INSTANCE!!
        }
    }
}