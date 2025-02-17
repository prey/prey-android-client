/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager

import com.prey.events.Event
import com.prey.events.manager.EventManagerRunner
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.services.PreySecureService

import org.json.JSONObject
import java.util.Date

/**
 * BroadcastReceiver that listens for the ACTION_CLOSE_SYSTEM_DIALOGS intent and disables power options on the device.
 */
class PreyDisablePowerOptionsReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    override fun onReceive(context: Context, intent: Intent) {
        val disablePowerOptions = PreyConfig.getInstance(context).isDisablePowerOptions()
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        PreyLogger.d(
            
                "PreyDisablePowerOptionsReceiver disablePowerOptions:${disablePowerOptions} canDrawOverlays: ${canDrawOverlays}"

        )
        if (canDrawOverlays && disablePowerOptions) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS" == intent.action) {
                val bundle = intent.extras
                if (bundle != null) {
                    for (key in bundle.keySet()) {
                        val value = bundle[key]
                        PreyLogger.d(
                                "PreyDisablePowerOptionsReceiver disablePowerOptions key:${key} value:${value}"
                        )
                    }
                }
                val flag =
                    (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).inKeyguardRestrictedInputMode()
                try {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val isScreenOn = pm.isScreenOn
                    val reason = intent.getStringExtra("reason")
                    if (isScreenOn && reason != null) {
                        var extra = intent.getStringExtra(stringExtra)
                        PreyLogger.d(
                                "PreyDisablePowerOptionsReceiver reason:${reason} flag:${flag} extra:${extra}"
                        )
                        val time = PreyConfig.getInstance(context).getTimeSecureLock()
                        val now = Date().time
                        PreyLogger.d(
                                "PreyDisablePowerOptionsReceiver time:${time} now:${now} <${ (now < time)}",
                        )
                        if (now < time) {
                            extra = ""
                        }
                        if (extra == null) {
                            val intentClose = Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS")
                            intentClose.putExtra(stringExtra, stringExtra)
                            context.sendBroadcast(intentClose)
                            val pinNumber = PreyConfig.getInstance(context).getPinNumber()
                            val isOpenSecureService =
                                PreyConfig.getInstance(context).isOpenSecureService()
                            PreyLogger.d(
                                    "PreyDisablePowerOptionsReceiver pinNumber:${pinNumber} isOpenSecureService:${isOpenSecureService}"
                            )
                            if ("globalactions" == reason && pinNumber != null && "" != pinNumber && pinNumber.length == 4) {
                                PreyLogger.d("pinNumber:$pinNumber")
                                PreyConfig.getInstance(context).setPinActivated(pinNumber)
                                if (!isOpenSecureService) {
                                    PreyLogger.d("open PreySecureService")
                                    PreyConfig.getInstance(context).setViewSecure(true)
                                    val intentLock = Intent(context, PreySecureService::class.java)
                                    context.startService(intentLock)
                                    object : Thread() {
                                        override fun run() {
                                            try {
                                                val info = JSONObject()
                                                info.put("PIN", pinNumber)
                                                val event =
                                                    Event(Event.ANDROID_LOCK_PIN, info.toString())
                                                Thread(EventManagerRunner(context, event)).start()
                                            } catch (e: Exception) {
                                                PreyLogger.e("Error send Lock:" + e.message, e)
                                            }
                                        }
                                    }.start()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    PreyLogger.e("error:" + e.message, e)
                }
            }
        }
    }

    companion object {
        var stringExtra: String = "prey"
    }
}