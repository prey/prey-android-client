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
import android.os.Build
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.prey.actions.aware.AwareController
import com.prey.actions.aware.AwareScheduled
import com.prey.actions.location.daily.DailyLocationScheduled
import com.prey.actions.triggers.TriggerController
import com.prey.activities.LoginActivity
import com.prey.beta.actions.PreyBetaController
import com.prey.events.factories.EventFactory
import com.prey.events.receivers.EventReceiver
import com.prey.net.PreyWebServices
import com.prey.services.PreyDisablePowerOptionsService
import com.prey.workers.PreyLocationWorkManager
import com.prey.workers.PreyActionsWorkManager
import java.util.Date

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
        MultiDex.install(this)
    }

    /**
     * Called when the application is created.
     */
    override fun onCreate() {
        super.onCreate()
        try {
            val unlockPass = PreyConfig.getInstance(applicationContext).getUnlockPass()
            if (unlockPass != null && "" != unlockPass) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                applicationContext.startActivity(intent)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error call intent LoginActivity: ${e.message}", e)
        }
        run(this)
        runReceiver(this)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            PreyLogger.e("Error FirebaseApp: ${e.message}", e)
        }
        PreyBetaController.getInstance().startPrey(applicationContext)
    }

    /**
     * Runs the main application logic.
     * @param context The application context.
     */
    fun run(context: Context) {
        try {
            PreyLogger.d("__________________")
            PreyLogger.i("Application launched!")
            PreyLogger.d("__________________")
            PreyConfig.getInstance(context).setReportNumber(0);
            val apiKey = PreyConfig.getInstance(context).getApiKey()
            val deviceKey = PreyConfig.getInstance(context).getDeviceId()
            PreyConfig.getInstance(context).setAwareDate("")
            PreyConfig.getInstance(context).initTimeC2dm()
            PreyLogger.d("apiKey: ${apiKey}")
            PreyLogger.d("deviceKey: ${deviceKey}")
            PreyLogger.d(
                "InstallationDate: ${
                    PreyConfig.getInstance(context).getInstallationDate()
                }"
            )
            if (PreyConfig.getInstance(context).getInstallationDate() == 0L) {
                PreyConfig.getInstance(context).setInstallationDate(Date().time)
            }
            val sessionId = PreyUtils.randomAlphaNumeric(16)
            PreyLogger.d("#######sessionId: ${sessionId}")
            PreyConfig.getInstance(context).setSessionId(sessionId)
            val missing = PreyConfig.getInstance(context).isMissing()
            PreyLogger.d("missing: ${missing}")
            if (deviceKey != null && "" != deviceKey) {
                object : Thread() {
                    override fun run() {
                        PreyConfig.getInstance(context).registerC2dm()
                        PreyWebServices.getInstance().getProfile(context)
                        val initName = PreyWebServices.getInstance().getNameDevice(context)
                        if (initName != null && "" != initName) {
                            PreyLogger.d("initName: ${initName}")
                            PreyConfig.getInstance(context).setDeviceName(initName)
                        }
                        PreyStatus.getInstance().initConfig(context)
                        val accessCoarseLocation = PreyPermission.canAccessCoarseLocation(context)
                        val accessFineLocation = PreyPermission.canAccessFineLocation(context)
                        val canAccessBackgroundLocation =
                            PreyPermission.canAccessBackgroundLocation(context)
                        val isGooglePlayServicesAvailable =
                            PreyUtils.isGooglePlayServicesAvailable(context)
                        if (isGooglePlayServicesAvailable && (accessCoarseLocation || accessFineLocation) && canAccessBackgroundLocation) {
                            Thread(Runnable {
                                PreyConfig.getInstance(context).setLocation(null);
                                AwareController.getInstance().initUpdateLocation(context)
                                AwareController.getInstance().registerGeofence(context)
                            }).start()
                            PreyLocationWorkManager.getInstance().locationWork(context)
                            AwareScheduled.getInstance().start(context)
                            DailyLocationScheduled.getInstance().start(context)
                        }
                        PreyActionsWorkManager.getInstance().actionsWork(context)
                        PreyScheduled.getInstance().start(context)
                        TriggerController.getInstance().run(context)
                        if (missing) {
                            if (PreyConfig.getInstance(context)
                                    .getIntervalReport() != null && "" != PreyConfig.getInstance(
                                    context
                                ).getIntervalReport()
                            ) {
                                // ReportScheduled.getInstance(context)!!.run()
                                // ReportService().run(context)
                            }
                        }
                        if (!PreyConfig.getInstance(context).isChromebook()) {
                            if (PreyConfig.getInstance(context).isDisablePowerOptions()) {
                                try {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                                        context.startService(
                                            Intent(
                                                context,
                                                PreyDisablePowerOptionsService::class.java
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    PreyLogger.e(
                                        "error startService PreyDisablePowerOptionsService : ${e.message}",
                                        e
                                    )
                                }
                            }
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error PreyApp:${e.message}", e)
        }
    }

    /**
     * Registers the event receiver for various system intents.
     *
     * @param context The application context.
     */
    private fun runReceiver(context: Context?) {
        val ACTION_POWER_CONNECTED = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        registerReceiver(eventReceiver, ACTION_POWER_CONNECTED)
        val ACTION_POWER_DISCONNECTED = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(eventReceiver, ACTION_POWER_DISCONNECTED)
        val ACTION_BATTERY_LOW = IntentFilter(Intent.ACTION_BATTERY_LOW)
        registerReceiver(eventReceiver, ACTION_BATTERY_LOW)
        val LOCATION_MODE_CHANGED = IntentFilter(EventFactory.LOCATION_MODE_CHANGED)
        registerReceiver(eventReceiver, LOCATION_MODE_CHANGED)
        val LOCATION_PROVIDERS_CHANGED = IntentFilter(EventFactory.LOCATION_PROVIDERS_CHANGED)
        registerReceiver(eventReceiver, LOCATION_PROVIDERS_CHANGED)
        val WIFI_STATE_CHANGED = IntentFilter(EventFactory.WIFI_STATE_CHANGED)
        registerReceiver(eventReceiver, WIFI_STATE_CHANGED)
        val USER_PRESENT = IntentFilter(EventFactory.USER_PRESENT)
        registerReceiver(eventReceiver, USER_PRESENT)
    }

    /**
     * Sets the installation data for the application.
     *
     * @param conversionData A map of conversion data.
     */
    fun setInstallData(conversionData: Map<String?, String>) {
        if (sessionCount == 0) {
            val install_type = "Install Type: ${conversionData["af_status"]}\n"
            val media_source = "Media Source: ${conversionData["media_source"]}\n"
            val install_time = "Install Time(GMT): ${conversionData["install_time"]}\n"
            val click_time = "Click Time(GMT): ${conversionData["click_time"]}\n"
            val is_first_launch = "Is First Launch: ${conversionData["is_first_launch"]}\n"
            InstallConversionData += install_type.plus(media_source).plus(install_time)
                .plus(click_time).plus(is_first_launch)
            sessionCount++
        }
    }

    companion object {
        var InstallConversionData: String = ""
        var sessionCount: Int = 0
    }
}