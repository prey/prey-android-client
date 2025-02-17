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
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.WindowManager

import com.prey.actions.observer.ActionResult
import com.prey.activities.CloseActivity
import com.prey.activities.PasswordHtmlActivity
import com.prey.activities.PasswordNativeActivity
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.events.Event
import com.prey.events.manager.EventManagerRunner
import com.prey.exceptions.PreyException
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.net.PreyWebServices
import com.prey.services.AppAccessibilityService
import com.prey.services.CheckLockActivated
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService

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
            val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
            val jobId = parameters?.getString(PreyConfig.JOB_ID)
            val unlockPass = parameters?.getString(PreyConfig.UNLOCK_PASS)!!
            val lockMessage = parameters?.getString(PreyConfig.LOCK_MESSAGE) ?: ""
            val reason: String? = null
            // Set lock settings
            if (jobId != null) {
                PreyConfig.getInstance(context).setJobIdLock(jobId)
            }
            PreyConfig.getInstance(context).setUnlockPass(unlockPass)
            PreyConfig.getInstance(context).setLockMessage(lockMessage)
            // Initiate the lock process
            lock(context, unlockPass, messageId, reason, jobId)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
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
            val messageId = parameters?.getString(PreyConfig.MESSAGE_ID)
            val jobId = parameters?.getString(PreyConfig.JOB_ID)
            var reason = "{\"origin\":\"panel\"}"
            if (jobId != null) {
                reason = "{\"device_job_id\":\"$jobId\",\"origin\":\"panel\"}"
            }
            // Reset lock settings
            PreyConfig.getInstance(context).setLockMessage("")
            PreyConfig.getInstance(context).setLock(false)
            PreyConfig.getInstance(context).deleteUnlockPass()
            // Handle lock stop process based on device version
            if (PreyConfig.getInstance(context).isMarshmallowOrAbove()) {
                // Handle Marshmallow and above devices
                sendStopNotification(context, messageId, reason)
                handleAccessibilityAndOverlay(context)
            } else {
                // Handle pre-Marshmallow devices
                handlePreMarshmallow(context)
            }
        } catch (e: Exception) {
            // Handle any exceptions that occur during the lock stop process
            handleException(context, e)
        }
    }

    /**
     * Handles an exception that occurred during the lock process.
     *
     * @param context The application context.
     * @param e The exception that occurred.
     */
    private fun handleException(context: Context, e: Exception) {
        PreyLogger.e("Error:${e.message}", e)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            context,
            UtilJson.makeMapParam("start", "lock", "failed", e.message)
        )
    }

    /**
     * Handles the lock process for pre-Marshmallow devices.
     *
     * This function changes the password and locks the device, then sends a notification
     * to the server indicating that the lock process has stopped.
     *
     * @param context The application context.
     */
    private fun handlePreMarshmallow(context: Context) {
        try {
            FroyoSupport.getInstance(context).changePasswordAndLock("", true)
            val screenLock =
                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    PreyConfig.TAG
                )
            screenLock.acquire()
            screenLock.release()
            Thread.sleep(2000)
            val reason = "{\"origin\":\"panel\"}"
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "lock", "stopped", reason)
            )
        } catch (e: Exception) {
            throw PreyException(e)
        }
    }

    /**
     * Handles the accessibility and overlay settings for the lock process.
     *
     * This function checks if the accessibility service is enabled and if the app can draw overlays.
     * If either condition is true, it removes the lock view and starts the CloseActivity.
     * Otherwise, it calls the handleNoAccessibilityAndOverlay function.
     *
     * @param context The application context.
     */
    private fun handleAccessibilityAndOverlay(context: Context) {
        Thread.sleep(2000)
        val canAccessibility = PreyPermission.isAccessibilityServiceEnabled(context)
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        if (canDrawOverlays || canAccessibility) {
            if (canDrawOverlays) {
                removeLockView(context)
            }
            val intentClose = Intent(context, CloseActivity::class.java)
            intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentClose)
        } else {
            handleNoAccessibilityAndOverlay(context)
        }
    }

    /**
     * Removes the lock view from the window manager.
     *
     * @param context The application context.
     */
    private fun removeLockView(context: Context) {
        try {
            val view = PreyConfig.getInstance(context).viewLock
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (wm != null && view != null) {
                wm.removeView(view)
                PreyConfig.getInstance(context).viewLock = null
            } else {
                Process.killProcess(Process.myPid())
            }
        } catch (e: Exception) {
            Process.killProcess(Process.myPid())
        }
    }

    /**
     * Handles the case where accessibility and overlay are not available.
     *
     * This function changes the password and locks the device, then sends a notification
     * to the server indicating that the lock process has stopped.
     *
     * @param context The application context.
     */
    private fun handleNoAccessibilityAndOverlay(context: Context) {
        try {
            FroyoSupport.getInstance(context).changePasswordAndLock("", true)
            val screenLock =
                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    PreyConfig.TAG
                )
            screenLock.acquire()
            screenLock.release()
            Thread.sleep(2000)
            val reason = "{\"origin\":\"panel\"}"
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context,
                UtilJson.makeMapParam("start", "lock", "stopped", reason)
            )
        } catch (e: Exception) {
            throw PreyException(e)
        }
    }

    /**
     * Sends a stop notification to the server.
     *
     * @param context The application context.
     * @param messageId The message ID.
     * @param reason The reason for the stop notification.
     */
    private fun sendStopNotification(context: Context, messageId: String?, reason: String) {
        Thread.sleep(1000)
        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
            context,
            "processed",
            messageId,
            UtilJson.makeMapParam("start", "lock", "stopped", reason)
        )
    }

    /**
     * Locks the device with the given unlock password, message ID, reason, and device job ID.
     *
     * This function sets the unlock password, enables the lock, and starts the necessary services
     * to lock the device. It also sends a notification to the server with the result of the lock action.
     *
     * @param context The application context.
     * @param unlock The unlock password.
     * @param messageId The message ID.
     * @param reason The reason for locking the device.
     * @param deviceJobId The device job ID.
     */
    fun lock(
        context: Context,
        unlock: String,
        messageId: String?,
        reason: String?,
        deviceJobId: String?
    ) {
        PreyLogger.d(

            "lock unlock:${unlock} messageId:${messageId} reason:${reason}"
        )
        PreyConfig.getInstance(context).setUnlockPass(unlock)
        PreyConfig.getInstance(context).setLock(true)
        PreyLogger.d("lock 1")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val accessibility = PreyPermission.isAccessibilityServiceEnabled(context)
            val canDrawOverlays = PreyPermission.canDrawOverlays(context)
            if (canDrawOverlays || accessibility) {
                if (canDrawOverlays) {
                    var intentPreyLock: Intent? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PreyLogger.d("lock 2")
                        intentPreyLock = Intent(context, PreyLockHtmlService::class.java)
                    } else {
                        PreyLogger.d("lock 3")
                        intentPreyLock = Intent(context, PreyLockService::class.java)
                    }
                    context.startService(intentPreyLock)
                    val intentCheckLock = Intent(context, CheckLockActivated::class.java)
                    context.startService(intentCheckLock)
                }
                if (accessibility) {
                    PreyLogger.d("lock 4")
                    PreyConfig.getInstance(context).setOverLock(false)
                    val intentAccessibility = Intent(context, AppAccessibilityService::class.java)
                    context.startService(intentAccessibility)
                    var intentPasswordActivity: Intent? = null
                    intentPasswordActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Intent(context, PasswordHtmlActivity::class.java)
                    } else {
                        Intent(context, PasswordNativeActivity::class.java)
                    }
                    intentPasswordActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentPasswordActivity)
                }
            } else {
                PreyLogger.d("lock 5")
                lockWhenYouNocantDrawOverlays(context)
            }
        } else {
            PreyLogger.d("lock 6")
            lockOld(context)
        }
        Thread {
            try {
                Thread.sleep(2000)
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    context,
                    "processed",
                    messageId,
                    UtilJson.makeMapParam("start", "lock", "started", reason)
                )
            } catch (e: Exception) {
                PreyLogger.e("Error sendNotifyAction:" + e.message, e)
            }
        }.start()
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
        Thread {
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
                        AppAccessibilityService::class.java
                    )
                    context.stopService(intentAccessibility)

                    object : Thread() {
                        override fun run() {
                            val jobIdLock = PreyConfig.getInstance(context).getJobIdLock()
                            var reason = "{\"origin\":\"user\"}"
                            if (jobIdLock != null && "" != jobIdLock) {
                                reason =
                                    "{\"origin\":\"user\",\"device_job_id\":\"$jobIdLock\"}"
                                PreyConfig.getInstance(context).setJobIdLock("")
                            }
                            PreyWebServices.getInstance()
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
                    }.start()
                }
            }
        }.start()
    }

    /**
     * Locks the device when accessibility service is not enabled and the app cannot draw overlays.
     *
     * @param context The application context.
     */
    fun lockWhenYouNocantDrawOverlays(context: Context) {
        val isAccessibilityServiceEnabled = PreyPermission.isAccessibilityServiceEnabled(context)
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        val unlockPass = PreyConfig.getInstance(context).getUnlockPass()
        PreyLogger.d(
            "DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass: ${unlockPass} accessibility: ${isAccessibilityServiceEnabled} canDrawOverlays: ${canDrawOverlays}"
        )
        if (unlockPass != null && unlockPass.isNotEmpty()) {
            if (!canDrawOverlays(context) && !isAccessibilityServiceEnabled) {
                val isPatternSet = isPatternSet(context)
                val isPassOrPinSet = isPassOrPinSet(context)
                PreyLogger.d("CheckLockActivated isPatternSet:$isPatternSet")
                PreyLogger.d("CheckLockActivated  isPassOrPinSet:$isPassOrPinSet")
                if (isPatternSet || isPassOrPinSet) {
                    FroyoSupport.getInstance(context).lockNow()
                    Thread(EventManagerRunner(context, Event(Event.NATIVE_LOCK))).start()
                } else {
                    try {
                        FroyoSupport.getInstance(context).changePasswordAndLock(
                            PreyConfig.getInstance(context).getUnlockPass(),
                            true
                        )
                        Thread(EventManagerRunner(context, Event(Event.NATIVE_LOCK))).start()
                    } catch (e: Exception) {
                        PreyLogger.e("Error FroyoSupport changePasswordAndLock:${e.message}", e)
                    }
                }
            }
        }
    }

    /**
     * Locks the device using the old locking mechanism.
     *
     * This function checks if the accessibility service is enabled and if the app can draw overlays.
     * It then checks if a unlock password is set and if a pattern or PIN is set on the device.
     * If a pattern or PIN is set, it locks the device immediately. Otherwise, it changes the password and locks the device.
     *
     * @param context The application context.
     */
    fun lockOld(context: Context) {
        val accessibility = PreyPermission.isAccessibilityServiceEnabled(context)
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        val unlockPassword = PreyConfig.getInstance(context).getUnlockPass()
        PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass1:$unlockPassword accessibility:$accessibility canDrawOverlays:$canDrawOverlays")
        if (unlockPassword != null && unlockPassword.isNotEmpty()) {
            val isPatternSet = isPatternSet(context)
            val isPassOrPinSet = isPassOrPinSet(context)
            PreyLogger.d("CheckLockActivated isPatternSet:$isPatternSet")
            PreyLogger.d("CheckLockActivated  isPassOrPinSet:$isPassOrPinSet")
            if (isPatternSet || isPassOrPinSet) {
                FroyoSupport.getInstance(context)?.lockNow()
            } else {
                try {
                    FroyoSupport.getInstance(context)!!
                        .changePasswordAndLock(unlockPassword, true)
                } catch (e: Exception) {
                    PreyLogger.e("error locking device:${e.message}", e)
                }
            }
        }
    }

    /**
     * Checks if the app can draw overlays on the device.
     *
     * @param context The application context.
     * @return True if the app can draw overlays, false otherwise.
     */
    fun canDrawOverlays(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return Settings.canDrawOverlays(context)
    }

    /**
     * Checks if a pattern is set on the device.
     *
     * This function is currently not implemented.
     *
     * @param context The application context.
     * @return Always returns false.
     */
    fun isPatternSet(context: Context?): Boolean {
        return false
    }

    /**
     * Checks if a pass or pin is set on the device.
     *
     * This function uses the KeyguardManager to check if the device is secure.
     *
     * @param context The application context.
     * @return True if a pass or pin is set, false otherwise.
     */
    @TargetApi(16)
    fun isPassOrPinSet(context: Context): Boolean {
        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 16+
        return keyguardManager.isKeyguardSecure
    }

}