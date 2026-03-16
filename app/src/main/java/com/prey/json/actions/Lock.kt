/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
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
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.activities.CloseActivity
import com.prey.activities.PasswordHtmlActivity
import com.prey.activities.PasswordNativeActivity
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.events.Event
import com.prey.events.manager.EventManagerRunner
import com.prey.exceptions.PreyException
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServicesKt
import com.prey.services.AppAccessibilityService
import com.prey.services.CheckLockActivated
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Command target for handling device lock and unlock operations.
 *
 * This class processes remote commands to either lock the device with a custom message and password,
 * or to unlock it. It handles different locking mechanisms based on the Android version and
 * available permissions (like "Draw over other apps" and "Accessibility").
 *
 * The main entry point is the `execute` method, which delegates to `start` for locking
 * and `stop` for unlocking.
 *
 * Locking strategies include:
 * - Displaying an overlay window (`PreyLockService` or `PreyLockHtmlService`).
 * - Using Accessibility services to block user interaction.
 * - Using the Device Administration API to set a new password and lock the screen.
 *
 * Unlocking reverses these actions, removing overlays, resetting passwords, and restoring
 * normal device operation.
 */
object Lock : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private const val TARGET = "lock"

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> scope.launch { start(context, options) }
            CMD_STOP -> scope.launch { stop(context, options) }
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Initiates the device lock sequence by parsing options and calling the lock function.
     *
     * This function extracts locking parameters from a `JSONObject`, such as the message ID,
     * job ID, unlock password, and a custom lock message. It then saves these settings
     * to `PreyConfig` and triggers the actual locking mechanism by calling [lock].
     * If any error occurs during option parsing, it reports a failure status to the web service.
     *
     * @param context The application context.
     * @param options A `JSONObject` containing configuration for the lock action.
     *        Expected keys include:
     *        - [PreyConfig.MESSAGE_ID]: The unique identifier for the message.
     *        - [PreyConfig.JOB_ID]: The job ID associated with this lock action.
     *        - [PreyConfig.UNLOCK_PASS]: The password required to unlock the device.
     *        - [PreyConfig.LOCK_MESSAGE]: The custom message to display on the lock screen.
     */
    suspend fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Lock start options:${options}")
        val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
        val jobId = options.optString(PreyConfig.JOB_ID, null)
        val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
        try {
            var unlock: String? = null
            if (options.has(PreyConfig.UNLOCK_PASS)) {
                unlock = options.getString(PreyConfig.UNLOCK_PASS)
                PreyConfig.getPreyConfig(context).unlockPass = unlock
            }
            if (options.has(PreyConfig.LOCK_MESSAGE)) {
                val lockMessage: String = options.getString(PreyConfig.LOCK_MESSAGE)
                PreyConfig.getPreyConfig(context).lockMessage = lockMessage
            } else {
                PreyConfig.getPreyConfig(context).lockMessage = ""
            }
            lock(context, unlock, messageId, reason)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_FAILED, e.message, messageId)
        }
    }

    suspend fun stop(context: Context, options: JSONObject) {
        PreyLogger.d("Lock stop options:${options}")
        try {
            val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
            var reason = "{\"origin\":\"panel\"}"
            if (options.has(PreyConfig.JOB_ID)) {
                val jobId: String = options.getString(PreyConfig.JOB_ID)
                PreyLogger.d("jobId:${jobId}")
                reason = "{\"device_job_id\":\"${jobId}\",\"origin\":\"panel\"}"
            }
            val jobIdLock = PreyConfig.getPreyConfig(context).jobIdLock
            if (jobIdLock != null && "" != jobIdLock) {
                reason = "{\"device_job_id\":\"${jobIdLock}\",\"origin\":\"panel\"}"
                PreyConfig.getPreyConfig(context).jobIdLock = ""
            }
            PreyConfig.getPreyConfig(context).lockMessage = ""
            PreyConfig.getPreyConfig(context).setLock(false)
            PreyConfig.getPreyConfig(context).deleteUnlockPass()
            if (PreyConfig.getPreyConfig(context).isMarshmallowOrAbove) {
                val canAccessibility = PreyPermission.isAccessibilityServiceEnabled(context)
                val canDrawOverlays = PreyPermission.canDrawOverlays(context)
                if (canDrawOverlays || canAccessibility) {
                    if (canDrawOverlays) {
                        try {
                            val view = PreyConfig.getPreyConfig(context).viewLock
                            val wm =
                                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
                            if (wm != null && view != null) {
                                wm.removeView(view)
                                PreyConfig.getPreyConfig(context).viewLock = null
                            } else {
                                Process.killProcess(Process.myPid())
                            }
                        } catch (e: java.lang.Exception) {
                            Process.killProcess(Process.myPid())
                        }
                    }
                    val intentClose = Intent(context, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentClose)
                } else {
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
                        reason = "{\"origin\":\"panel\"}"

                    } catch (e: java.lang.Exception) {
                        throw PreyException(e)
                    }
                }
            } else {
                try {
                    if (!PreyConfig.getPreyConfig(context).isMarshmallowOrAbove()) {
                        FroyoSupport.getInstance(context).changePasswordAndLock("", true)
                        val screenLock =
                            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                PreyConfig.TAG
                            )
                        screenLock.acquire()
                        screenLock.release()
                    }
                    reason = "{\"origin\":\"panel\"}"
                } catch (e: java.lang.Exception) {
                    throw PreyException(e)
                }
            }
            Thread.sleep(2000)
            PreyWebServicesKt.notify(context, CMD_STOP, TARGET, STATUS_STOPPED, reason)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error:${e.message}", e)
            PreyWebServicesKt.notify(context, CMD_STOP, TARGET, STATUS_FAILED, e.message)
        }
    }

    suspend fun lock(
        context: Context,
        unlock: String?,
        messageId: String?,
        reason: String?
    ) {
        PreyLogger.d("lock unlock:${unlock} messageId:${messageId} reason:${reason}")
        PreyConfig.getPreyConfig(context).unlockPass = unlock
        PreyConfig.getPreyConfig(context).setLock(true)
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
                    PreyConfig.getPreyConfig(context).overLock = false
                    val intentAccessibility = Intent(context, AppAccessibilityService::class.java)
                    context.startService(intentAccessibility)
                    var intentPasswordActivity: Intent? = null
                    intentPasswordActivity = Intent(context, PasswordHtmlActivity::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    } else {
                        intentPasswordActivity = Intent(context, PasswordNativeActivity::class.java)
                    }
                    intentPasswordActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentPasswordActivity)
                }
                //Lock immediately, as if the lock screen timeout has expired
                FroyoSupport.getInstance(context).lockNow()
            } else {
                PreyLogger.d("lock 5")
                lockWhenYouNocantDrawOverlays(context)
            }
        } else {
            PreyLogger.d("lock 6")
            lockOld(context)
        }
        PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STARTED, reason)
    }

    fun lockWhenYouNocantDrawOverlays(context: Context) {
        val accessibility = PreyPermission.isAccessibilityServiceEnabled(context)
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        val unlockPass = PreyConfig.getPreyConfig(context).getUnlockPass()
        PreyLogger.d("lockWhenYouNocantDrawOverlays unlockPass: ${unlockPass} accessibility: ${accessibility} canDrawOverlays: ${canDrawOverlays}")
        val isAccessibilityServiceEnabled = PreyPermission.isAccessibilityServiceEnabled(context)
        if (unlockPass != null && "" != unlockPass) {
            if (!canDrawOverlays(context) && !isAccessibilityServiceEnabled) {
                val isPatternSet = isPatternSet(context)
                val isPassOrPinSet = isPassOrPinSet(context)
                PreyLogger.d("lockWhenYouNocantDrawOverlays isPatternSet:${isPatternSet}")
                PreyLogger.d("lockWhenYouNocantDrawOverlays isPassOrPinSet:${isPassOrPinSet}")
                if (isPatternSet || isPassOrPinSet) {
                    FroyoSupport.getInstance(context).lockNow()
                    Thread(EventManagerRunner(context, Event(Event.NATIVE_LOCK))).start()
                } else {
                    try {
                        FroyoSupport.getInstance(context).changePasswordAndLock(
                            unlockPass,
                            true
                        )
                        Thread(EventManagerRunner(context, Event(Event.NATIVE_LOCK))).start()
                    } catch (e: java.lang.Exception) {
                        PreyLogger.e("Error lockWhenYouNocantDrawOverlays:${e.message}", e)
                    }
                }
            }
        }
    }

    fun lockOld(context: Context) {
        val accessibility = PreyPermission.isAccessibilityServiceEnabled(context)
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        val unlockPass = PreyConfig.getPreyConfig(context).getUnlockPass()
        PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass:${unlockPass} accessibility:${accessibility} canDrawOverlays:${canDrawOverlays}")
        if (unlockPass != null && "" != unlockPass) {
            val isPatternSet = isPatternSet(context)
            val isPassOrPinSet = isPassOrPinSet(context)
            PreyLogger.d("CheckLockActivated isPatternSet:${isPatternSet}")
            PreyLogger.d("CheckLockActivated  isPassOrPinSet:${isPassOrPinSet}")
            if (isPatternSet || isPassOrPinSet) {
                FroyoSupport.getInstance(context).lockNow()
            } else {
                try {
                    FroyoSupport.getInstance(context)
                        .changePasswordAndLock(
                            PreyConfig.getPreyConfig(context).getUnlockPass(),
                            true
                        )
                } catch (e: java.lang.Exception) {
                    PreyLogger.e("error lockold:${e.message}", e)
                }
            }
        }
    }

    fun isPatternSet(context: Context?): Boolean {
        return false
    }

    @TargetApi(16)
    fun isPassOrPinSet(context: Context): Boolean {
        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 16+
        return keyguardManager.isKeyguardSecure()
    }

    fun canDrawOverlays(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return Settings.canDrawOverlays(context)
    }
}