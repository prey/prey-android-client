package com.prey.exceptions

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.prey.PreyConfig


class PreyFirebaseCrashlytics//initialize FirebaseCrashlytics
private constructor(ctx: Context) {



    private val crashlytics: FirebaseCrashlytics? = null

    init {
        FirebaseApp.initializeApp(ctx)
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("devicekey", PreyConfig.getInstance(ctx).getDeviceId()!!)
        crashlytics.setCustomKey("apikey", PreyConfig.getInstance(ctx).getApiKey()!!)
    }


    /**
     * Method to report an exception
     */
    fun recordException(throwable: Throwable) {
        crashlytics!!.recordException(throwable)
    }

    companion object {

        private var instance: PreyFirebaseCrashlytics? = null

        fun getInstance(ctx: Context): PreyFirebaseCrashlytics {
            if (instance == null) {
                instance = PreyFirebaseCrashlytics(ctx)
            }
            return instance!!
        }
    }
}