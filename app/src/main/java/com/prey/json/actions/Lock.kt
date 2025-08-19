/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent

import com.prey.actions.observer.ActionResult
import com.prey.activities.CloseActivity
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.actions.lock.LockAction
import com.prey.services.PreyAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.json.JSONObject

/**
 * Lock class responsible for handling device lock functionality.
 */
class Lock {

    /**
     * Starts the lock process.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON object containing lock parameters.
     */
    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        try {
            // Extract lock parameters from the JSON object
            var messageId: String? = null
            try {
                messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
                PreyLogger.d("messageId:${messageId}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var jobId: String? = null
            try {
                jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
                PreyLogger.d("jobId:${jobId}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var unlockPass: String? = null
            try {
                unlockPass = UtilJson.getStringValue(parameters, PreyConfig.UNLOCK_PASS)
                PreyLogger.d("unlockPass:${unlockPass}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var lockMessage: String? = null
            try {
                lockMessage = UtilJson.getStringValue(parameters, PreyConfig.LOCK_MESSAGE)
                PreyLogger.d("lockMessage:${lockMessage}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var reason: String? = null
            // Set lock settings
            if (jobId != null) {
                reason = "{\"device_job_id\":\"${jobId}\"}";
                PreyConfig.getInstance(context).setJobIdLock(jobId)
            }
            PreyConfig.getInstance(context).setUnlockPass(unlockPass!!)
            PreyConfig.getInstance(context).setLockMessage(lockMessage)
            // Initiate the lock process
            LockAction().start(context, messageId, unlockPass, reason)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "lock", "failed", e.message)
            )
        }
    }

    /**
     * Stops the lock process.
     *
     * @param context        The application context.
     * @param actionResults  The list of action results.
     * @param parameters     The JSON object containing lock parameters.
     */
    fun stop(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        try {
            // Extract lock parameters from the JSON object
            var messageId: String? = null
            try {
                messageId = UtilJson.getStringValue(parameters, PreyConfig.MESSAGE_ID)
                PreyLogger.d("messageId:${messageId}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var jobId: String? = null
            try {
                jobId = UtilJson.getStringValue(parameters, PreyConfig.JOB_ID)
                PreyLogger.d("jobId:${jobId}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var reason = "{\"origin\":\"panel\"}"
            if (jobId != null) {
                reason = "{\"device_job_id\":\"$jobId\",\"origin\":\"panel\"}"
            }
            // Reset lock settings
            PreyConfig.getInstance(context).setLockMessage("")
            PreyConfig.getInstance(context).setLock(false)
            PreyConfig.getInstance(context).deleteUnlockPass()
            LockAction().stop(context, messageId, reason)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "lock", "failed", e.message)
            )
        }
    }


    /**
     * @return true if pass or pin or pattern locks screen
     */
    @TargetApi(23)
    private fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 23+
        return keyguardManager.isDeviceSecure
    }

    /**
     * Sends an unlock request to the device.
     *
     * This function checks if the device has a valid unlock pass and if it's running on a Marshmallow or above device.
     * If the conditions are met, it clears the unlock pass, closes the current activity, stops the accessibility service,
     * and sends a notification to the server with the result of the unlock action.
     *
     * @param context The application context.
     */
    fun sendUnLock(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val unlockPass = PreyConfig.getInstance(context).getUnlockPass()
            PreyLogger.d("sendUnLock unlockPass:$unlockPass")
            if (unlockPass != null && "" != unlockPass) {
                if (PreyConfig.getInstance(context)
                        .isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(
                        context
                    )
                ) {
                    PreyLogger.d("sendUnLock nothing")
                } else {
                    PreyLogger.d("sendUnLock deleteUnlockPass")
                    PreyConfig.getInstance(context).setUnlockPass("")
                    val intentClose =
                        Intent(context, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentClose)
                    val intentAccessibility = Intent(
                        context,
                        PreyAccessibilityService::class.java
                    )
                    context.stopService(intentAccessibility)
                    CoroutineScope(Dispatchers.IO).launch {
                        val jobIdLock = PreyConfig.getInstance(context).getJobIdLock()
                        var reason = "{\"origin\":\"user\"}"
                        if (jobIdLock != null && "" != jobIdLock) {
                            reason =
                                "{\"origin\":\"user\",\"device_job_id\":\"$jobIdLock\"}"
                            PreyConfig.getInstance(context).setJobIdLock("")
                        }
                        PreyConfig.getInstance(context).getWebServices()
                            .sendNotifyActionResultPreyHttp(
                                context,
                                UtilJson.makeMapParam(
                                    "start",
                                    "lock",
                                    "stopped",
                                    reason
                                )
                            )
                    }
                }
            }
        }
    }

}