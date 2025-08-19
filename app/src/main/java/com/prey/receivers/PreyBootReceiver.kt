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
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This class is a BroadcastReceiver that listens for the android.intent.action.BOOT_COMPLETED intent,
 * which is broadcast after the system finishes booting.
 */
class PreyBootReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        PreyLogger.d("Boot finished. Starting Prey Boot Service")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                startReport(context)
                startRestrictions(context)
                startLock(context)
                startPrey(context)
                startRestrictions(context)
            }
        } else {
            PreyLogger.e("Received unexpected intent $intent", null)
        }
    }

    private suspend fun startReport(context: Context) {
        val interval = PreyConfig.getInstance(context).getIntervalReport()
        PreyLogger.d("interval:${interval}")
        if (interval != null && "" != interval) {
            Report().run(context, interval.toInt())
        }
    }

    private suspend fun startLock(context: Context) {
        try {
            val unlockPass = PreyConfig.getInstance(context).getUnlockPass()
            PreyLogger.d("unlockPass:$unlockPass")
            if (unlockPass != null && "" != unlockPass) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PreyPermission.canDrawOverlays(
                        context
                    )
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        context.startService(Intent(context, PreyLockHtmlService::class.java))
                    } else {
                        context.startService(Intent(context, PreyLockService::class.java))
                    }
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    private suspend fun startPrey(context: Context) {
        try {
            PreyBetaController.getInstance().startPrey(context)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
    }

    private suspend fun startRestrictions(context: Context) {
        // Get the RestrictionsManager instance
        val manager =
            context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        // Get the application restrictions
        val applicationRestrictions = manager.applicationRestrictions
        // Check if application restrictions are not null
        if (applicationRestrictions != null) {
            // Log the application restrictions
            PreyLogger.d(
                "RestrictionsReceiver restrictions applied: ${applicationRestrictions.toString()}"
            )
            // Handle the application restrictions
            RestrictionsReceiver().handleApplicationRestrictions(
                context,
                applicationRestrictions
            )
        }
    }

}