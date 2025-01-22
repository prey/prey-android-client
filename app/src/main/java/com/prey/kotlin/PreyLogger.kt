/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.kotlin

import android.util.Log

class PreyLogger {

    fun d(message: String?) {
        Log.i(com.prey.PreyConfig.TAG, "OSO: $message")
    }

    fun i(message: String?) {
        Log.i(PreyConfig.TAG, message!!)
    }

    fun e(message: String?, e: Throwable?) {
        if (e != null) Log.e(PreyConfig.TAG, message, e)
        else Log.e(PreyConfig.TAG, message!!)
    }
    companion object {
        fun i(message: String?) {
            Log.i(PreyConfig.TAG,"OSO_:$message")
        }
        fun d(message: String?) {
            Log.i(PreyConfig.TAG,"OSO_:$message")
        }
        fun e(message: String?, e: Throwable?) {
            Log.e(PreyConfig.TAG,"OSO_:$message",e)
        }
    }


}