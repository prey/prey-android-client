/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers.kotlin

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.prey.events.kotlin.Event
import com.prey.events.manager.kotlin.EventManagerRunner
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPermission
import com.prey.services.kotlin.PreySecureService
import org.json.JSONObject
import java.util.Date

class PreyDisablePowerOptionsReceiver : BroadcastReceiver() {
    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    override fun onReceive(context: Context, intent: Intent) {
        val disablePowerOptions = PreyConfig.getInstance(context).isDisablePowerOptions()
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        PreyLogger.d(
            String.format(
                "PreyDisablePowerOptionsReceiver disablePowerOptions:%s canDrawOverlays: %s",
                disablePowerOptions,
                canDrawOverlays
            )
        )
        if (canDrawOverlays && disablePowerOptions) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS" == intent.action) {
                val bundle = intent.extras
                if (bundle != null) {
                    for (key in bundle.keySet()) {
                        val value = bundle[key]
                        PreyLogger.d(
                            String.format(
                                "PreyDisablePowerOptionsReceiver disablePowerOptions key:%s value:%s",
                                key,
                                value
                            )
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
                            String.format(
                                "PreyDisablePowerOptionsReceiver reason:%s flag:%s extra:%s",
                                reason,
                                flag,
                                extra
                            )
                        )
                        val time = PreyConfig.getInstance(context).getTimeSecureLock()
                        val now = Date().time
                        PreyLogger.d(
                            String.format(
                                "PreyDisablePowerOptionsReceiver time:%s now:%s <%s",
                                time,
                                now,
                                (now < time)
                            )
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
                                String.format(
                                    "PreyDisablePowerOptionsReceiver pinNumber:%s isOpenSecureService:%s",
                                    pinNumber,
                                    isOpenSecureService
                                )
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