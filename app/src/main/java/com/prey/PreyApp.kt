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
import com.prey.actions.fileretrieval.FileretrievalController
import com.prey.actions.geofences.GeofenceController
import com.prey.actions.location.daily.LocationScheduled
import com.prey.actions.report.ReportScheduled
import com.prey.actions.report.ReportService
import com.prey.actions.triggers.TriggerController
import com.prey.activities.LoginActivity
import com.prey.beta.actions.PreyBetaController
import com.prey.events.factories.EventFactory
import com.prey.events.receivers.EventReceiver
import com.prey.net.PreyWebServices
import com.prey.preferences.RunBackgroundCheckBoxPreference
import com.prey.services.AwareJobService
import com.prey.services.PreyDisablePowerOptionsService
import com.prey.services.PreyJobService
import com.prey.workers.PreyWorker
import java.util.Date

class PreyApp : Application() {
    var mLastPause: Long = 0
    private val eventReceiver = EventReceiver()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

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
            PreyLogger.e(String.format("Error call intent LoginActivity: %s", e.message), e)
        }
        run(this)
        runReceiver(this)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error FirebaseApp: %s", e.message), e)
        }
        PreyBetaController.getInstance().startPrey(applicationContext)
    }

    fun run(ctx: Context) {
        try {
            mLastPause = 0
            PreyLogger.d("__________________")
            PreyLogger.i("Application launched!")
            PreyLogger.d("__________________")
            PreyConfig.getInstance(ctx).setReportNumber(0);
            val apiKey = PreyConfig.getInstance(ctx).getApiKey()
            val deviceKey = PreyConfig.getInstance(ctx).getDeviceId()
            PreyConfig.getInstance(ctx).setAwareDate("")
            PreyConfig.getInstance(ctx).initTimeC2dm()
            PreyLogger.d(String.format("apiKey: %s", apiKey))
            PreyLogger.d(String.format("deviceKey: %s", deviceKey))
            PreyLogger.d(
                String.format(
                    "InstallationDate: %s",
                    PreyConfig.getInstance(ctx).getInstallationDate()
                )
            )
            if (PreyConfig.getInstance(ctx).getInstallationDate() == 0L) {
                PreyConfig.getInstance(ctx).setInstallationDate(Date().time)
            }
            val sessionId = PreyUtils.randomAlphaNumeric(16)
            PreyLogger.d(String.format("#######sessionId: %s", sessionId))
            PreyConfig.getInstance(ctx).setSessionId(sessionId)
            val missing = PreyConfig.getInstance(ctx).isMissing()
            PreyLogger.d(String.format("missing: %b", missing))
            if (deviceKey != null && "" != deviceKey) {
                object : Thread() {
                    override fun run() {
                        PreyConfig.getInstance(ctx).registerC2dm()
                        PreyWebServices.getInstance().getProfile(ctx)
                        val initName = PreyWebServices.getInstance().getNameDevice(ctx)
                        if (initName != null && "" != initName) {
                            PreyLogger.d(String.format("initName: %s", initName))
                            PreyConfig.getInstance(ctx).setDeviceName(initName)
                        }
                        PreyStatus.getInstance().initConfig(ctx)
                        val accessCoarseLocation = PreyPermission.canAccessCoarseLocation(ctx)
                        val accessFineLocation = PreyPermission.canAccessFineLocation(ctx)
                        val canAccessBackgroundLocation =
                            PreyPermission.canAccessBackgroundLocation(ctx)
                        val verifyNotification = EventFactory.verifyNotification(ctx)
                        if (!verifyNotification) {
                            EventFactory.notification(ctx)
                        }
                        val isGooglePlayServicesAvailable =
                            PreyUtils.isGooglePlayServicesAvailable(ctx)
                        if (isGooglePlayServicesAvailable && (accessCoarseLocation || accessFineLocation) && canAccessBackgroundLocation) {
                            GeofenceController.getInstance().run(ctx)
                            AwareController.getInstance().init(ctx)
                            AwareScheduled.getInstance(ctx)!!.run()
                            LocationScheduled.getInstance().run(ctx)
                            PreyWorker.getInstance().startPeriodicWork(ctx)
                        }
                        FileretrievalController.getInstance().run(ctx)
                        TriggerController.getInstance().run(ctx)
                        if (missing) {
                            if (PreyConfig.getInstance(ctx)
                                    .getIntervalReport() != null && "" != PreyConfig.getInstance(
                                    ctx
                                ).getIntervalReport()
                            ) {
                                ReportScheduled.getInstance(ctx)!!.run()
                                ReportService().run(ctx)
                            }
                        }
                        if (!PreyConfig.getInstance(ctx).isChromebook()) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    PreyJobService.schedule(ctx)
                                    if (isGooglePlayServicesAvailable) {
                                        AwareJobService.schedule(ctx)
                                    }
                                }
                            } catch (e: Exception) {
                                PreyLogger.e(
                                    String.format(
                                        "error jobService.schedule : %s",
                                        e.message
                                    ), e
                                )
                            }
                            if (PreyConfig.getInstance(ctx).isRunBackground()) {
                                RunBackgroundCheckBoxPreference.notifyReady(ctx)
                            }
                            if (PreyConfig.getInstance(ctx).isDisablePowerOptions()) {
                                try {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                                        ctx.startService(
                                            Intent(
                                                ctx,
                                                PreyDisablePowerOptionsService::class.java
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    PreyLogger.e(
                                        String.format(
                                            "error startService PreyDisablePowerOptionsService : %s",
                                            e.message
                                        ), e
                                    )
                                }
                            }
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error PreyApp: %s", e.message), e)
        }
    }

    fun runReceiver(ctx: Context?) {
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

    companion object {
        var InstallConversionData: String = ""
        var sessionCount: Int = 0

        fun setInstallData(conversionData: Map<String?, String>) {
            if (sessionCount == 0) {
                val install_type = "Install Type: " + conversionData["af_status"] + "\n"
                val media_source = "Media Source: " + conversionData["media_source"] + "\n"
                val install_time = "Install Time(GMT): " + conversionData["install_time"] + "\n"
                val click_time = "Click Time(GMT): " + conversionData["click_time"] + "\n"
                val is_first_launch = "Is First Launch: " + conversionData["is_first_launch"] + "\n"
                InstallConversionData += install_type + media_source + install_time + click_time + is_first_launch
                sessionCount++
            }
        }
    }
}