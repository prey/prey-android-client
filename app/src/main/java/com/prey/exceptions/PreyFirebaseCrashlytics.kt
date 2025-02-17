package com.prey.exceptions

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

import com.prey.PreyConfig

/**
 * A private constructor class for handling Firebase Crashlytics.
 * This class is responsible for initializing Firebase Crashlytics and reporting exceptions.
 */
class PreyFirebaseCrashlytics private constructor(context: Context) {

    // Initialize Firebase Crashlytics instance
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    /**
     * Initializes Firebase Crashlytics with the provided context.
     * Sets custom keys for device ID and API key.
     */
    init {
        FirebaseApp.initializeApp(context)
        // Set custom keys for device ID and API key
        crashlytics.setCustomKey("devicekey", PreyConfig.getInstance(context).getDeviceId()!!)
        crashlytics.setCustomKey("apikey", PreyConfig.getInstance(context).getApiKey()!!)
    }

    /**
     * Reports an exception to Firebase Crashlytics.
     *
     * @param throwable The exception to be reported.
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    companion object {
        private var instance: PreyFirebaseCrashlytics? = null
        fun getInstance(context: Context): PreyFirebaseCrashlytics {
            return instance ?: synchronized(this) {
                instance ?: PreyFirebaseCrashlytics(context).also { instance = it }
            }
        }
    }
}