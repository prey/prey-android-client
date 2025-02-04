/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.RestrictionsManager
import android.os.Build
import com.prey.beta.actions.PreyBetaController
import com.prey.json.actions.Report
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.preferences.RunBackgroundCheckBoxPreference
import com.prey.services.PreyDisablePowerOptionsService
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService

class PreyBootController : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.d("Boot finished. Starting Prey Boot Service")
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            val interval = PreyConfig.getInstance(context).getIntervalReport()
            if (interval != null && "" != interval) {
                Report.run(context, interval.toInt())
            }
            val ctx = context
            object : Thread() {
                override fun run() {
                    try {
                        val disablePowerOptions =
                            PreyConfig.getInstance(ctx).isDisablePowerOptions()
                        if (disablePowerOptions) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                                ctx.startService(
                                    Intent(
                                        ctx,
                                        PreyDisablePowerOptionsService::class.java
                                    )
                                )
                            }
                        } else {
                            ctx.stopService(Intent(ctx, PreyDisablePowerOptionsService::class.java))
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                    try {
                        val runBackground = PreyConfig.getInstance(ctx).isRunBackground()
                        if (runBackground) {
                            RunBackgroundCheckBoxPreference.notifyReady(ctx)
                        } else {
                            RunBackgroundCheckBoxPreference.notifyCancel(ctx)
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                }
            }.start()
            object : Thread() {
                override fun run() {
                    try {
                        val unlockPass = PreyConfig.getInstance(ctx).getUnlockPass()
                        PreyLogger.d("unlockPass:$unlockPass")
                        if (unlockPass != null && "" != unlockPass) {
                            if (PreyConfig.getInstance(ctx).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(
                                    ctx
                                )
                            ) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    ctx.startService(Intent(ctx, PreyLockHtmlService::class.java))
                                } else {
                                    ctx.startService(Intent(ctx, PreyLockService::class.java))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                }
            }.start()
            object : Thread() {
                override fun run() {
                    try {
                        PreyBetaController.getInstance().startPrey(ctx)
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                }
            }.start()
            object : Thread() {
                override fun run() {
                    // Get the RestrictionsManager instance
                    val manager =
                        context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
                    // Get the application restrictions
                    val applicationRestrictions = manager.applicationRestrictions
                    // Check if application restrictions are not null
                    if (applicationRestrictions != null) {
                        // Log the application restrictions
                        PreyLogger.d(
                            String.format(
                                "RestrictionsReceiver restrictions applied: %s",
                                applicationRestrictions.toString()
                            )
                        )
                        // Handle the application restrictions
                        RestrictionsReceiver.handleApplicationRestrictions(
                            context,
                            applicationRestrictions
                        )
                    }
                }
            }.start()
        } else {
            PreyLogger.e("Received unexpected intent $intent", null)
        }
    }
}
