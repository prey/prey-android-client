package com.prey

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.work.WorkManager
import com.prey.actions.aware.AwareInitialLocationProvider
import com.prey.actions.fileretrieval.FileretrievalController
import com.prey.actions.location.daily.DailyLocationUtil.enqueueDailyCheck
import com.prey.actions.report.ReportScheduled
import com.prey.actions.report.ReportService
import com.prey.actions.triggers.TriggerController
import com.prey.events.factories.EventFactory
import com.prey.events.receivers.EventReceiver
import com.prey.net.PreyWebServices
import com.prey.net.PreyWebServicesKt.getNameDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date

class PreyApp : Application(){

    private val eventReceiver = EventReceiver()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    var mLastPause: Long = 0
    override fun onCreate() {
        super.onCreate()

        // Este código se ejecuta una sola vez al iniciar la app
        scope.launch {run()  }
    }

    private suspend fun run() {
        val context=applicationContext
        val config=PreyConfig.getPreyConfig(context)
        mLastPause = 0
        PreyLogger.d("__________________")
        PreyLogger.i("Application launched!")
        PreyLogger.d("__________________")
        config.setReportNumber(0)
        val apiKey = config.getApiKey()
        val deviceKey = config.getDeviceId()
        config.setAwareDate("")
        config.initTimeC2dm()
        PreyLogger.d(String.format("apiKey: %s", apiKey))
        PreyLogger.d(String.format("deviceKey: %s", deviceKey))
        PreyLogger.d(String.format("InstallationDate: %s", config.getInstallationDate()))
        if (config.getInstallationDate() == 0L) {
            config.setInstallationDate(Date().getTime())
        }
        val sessionId = PreyUtils.randomAlphaNumeric(16)
        PreyLogger.d(String.format("#######sessionId: %s", sessionId))
        config.setSessionId(sessionId)
        val missing = config.isMissing()
        PreyLogger.d(String.format("missing: %b", missing))
        WorkManager.getInstance(context).cancelAllWork()
        if (deviceKey != null && "" != deviceKey) {
            config.registerC2dm()
            PreyWebServices.getInstance().getProfile(context)
            val nameDevice = getNameDevice(context)
            if (!nameDevice.isNullOrEmpty()) {
                PreyLogger.d(String.format("nameDevice: %s", nameDevice))
                config.deviceName = nameDevice
            }
            PreyStatus.getInstance().initConfig(context)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                AwareInitialLocationProvider(context).init()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enqueueDailyCheck(context)
                }
            }
            FileretrievalController.getInstance().run(context)
            TriggerController.getInstance().run(context)
            if (missing) {
                if (config.getIntervalReport() != null && "" != config.getIntervalReport()) {
                    ReportScheduled.getInstance(context).run()
                    ReportService().run(context)
                }
            }
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
}