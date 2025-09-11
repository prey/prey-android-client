/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.firebase.FirebaseApp

import com.prey.actions.geofencing.GeofenceManager
import com.prey.activities.LoginActivity
import com.prey.beta.actions.PreyBetaController
import com.prey.events.factories.EventFactory
import com.prey.events.receivers.EventReceiver
import com.prey.workers.PreyLocationWorkManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * The main application class for Prey.
 */
class PreyApp : Application() {

    /**
     * The event receiver instance for handling various system events.
     */
    private val eventReceiver = EventReceiver()

    /**
     * Called when the application is attached to the base context.
     * @param base The base context.
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    /**
     * Called when the application is created.
     */
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            PreyLogger.e("FirebaseError: ${e.message}", e)
        }
        try {
            val unlockPass = PreyConfig.getInstance(applicationContext).getUnlockPass()
            if (unlockPass != null && "" != unlockPass) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                applicationContext.startActivity(intent)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        initialize(this)
        runReceiver()
        PreyBetaController.getInstance().startPrey(applicationContext)
    }

    /**
     * Runs the main application logic.
     * @param context The application context.
     */
    fun initialize(context: Context) {
        try {
            PreyLogger.d("__________________")
            PreyLogger.i("Application launched!")
            PreyLogger.d("__________________")
            PreyConfig.getInstance(context).setReportNumber(0);
            val apiKey = PreyConfig.getInstance(context).getApiKey()
            val deviceKey = PreyConfig.getInstance(context).getDeviceId()
            PreyConfig.getInstance(context).setAwareDate("")
            PreyConfig.getInstance(context).initTimeC2dm()
            PreyLogger.d("apiKey: $apiKey")
            PreyLogger.d("deviceKey: $deviceKey")
            val sessionId = PreyUtils.randomAlphaNumeric(16)
            PreyLogger.d("#######sessionId: $sessionId")
            PreyConfig.getInstance(context).setSessionId(sessionId)
            val missing = PreyConfig.getInstance(context).isMissing()
            PreyLogger.d("missing: $missing")
            if (deviceKey != null && "" != deviceKey) {
                CoroutineScope(Dispatchers.IO).launch {
                    PreyLogger.d("__________________")
                    PreyLogger.i("Application CoroutineScope")
                    PreyLogger.d("__________________")
                    PreyConfig.getInstance(context).registerC2dm()
                    PreyConfig.getInstance(context).getWebServices().getProfile(context)
                    val initName =
                        PreyConfig.getInstance(context).getWebServices().getNameDevice(context)
                    if (initName != null && "" != initName) {
                        PreyLogger.d("initName: $initName")
                        PreyConfig.getInstance(context).setDeviceName(initName)
                    }
                    PreyStatus.getInstance().initConfig(context)
                    val accessCoarseLocation = PreyPermission.canAccessCoarseLocation(context)
                    val accessFineLocation = PreyPermission.canAccessFineLocation(context)
                    val canAccessBackgroundLocation =
                        PreyPermission.canAccessBackgroundLocation(context)
                    val isGooglePlayServicesAvailable =
                        PreyUtils.isGooglePlayServicesAvailable(context)
                    if (isGooglePlayServicesAvailable && canAccessBackgroundLocation) {
                        GeofenceManager.getInstance(context).initGeofence()
                        PreyLocationWorkManager.getInstance().locationWork(context)
                    }
                    //PreyActionsWorkManager.getInstance().actionsWork(context)
                    // PreyScheduled.getInstance().start(context)
                    // TriggerController.getInstance().run(context)
                    if (missing) {
                        if (PreyConfig.getInstance(context)
                                .getIntervalReport() != null && "" != PreyConfig.getInstance(
                                context
                            ).getIntervalReport()
                        ) {
                            //ReportScheduled.getInstance(context).run()
                            //   ReportService().run(context)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    /**
     * Registers the event receiver for various system intents.
     */
    private fun runReceiver() {
        registerReceiver(eventReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        registerReceiver(eventReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
        registerReceiver(eventReceiver, IntentFilter(Intent.ACTION_BATTERY_LOW))
        registerReceiver(eventReceiver, IntentFilter(EventFactory.LOCATION_MODE_CHANGED))
        registerReceiver(eventReceiver, IntentFilter(EventFactory.LOCATION_PROVIDERS_CHANGED))
        registerReceiver(eventReceiver, IntentFilter(EventFactory.WIFI_STATE_CHANGED))
        registerReceiver(eventReceiver, IntentFilter(EventFactory.USER_PRESENT))
    }

}