package com.prey.actions.lock

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
import com.prey.json.UtilJson
import com.prey.services.CheckLockService
import com.prey.services.PreyAccessibilityService
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockAction {

    /**
     * Locks the device with the given unlock password, message ID, reason, and device job ID.
     *
     * This function sets the unlock password, enables the lock, and starts the necessary services
     * to lock the device. It also sends a notification to the server with the result of the lock action.
     *
     * @param context The application context.
     * @param messageId The message ID.
     * @param unlock The unlock password.
     * @param reason The reason for locking the device.
     */

    fun start(
        context: Context,
        messageId: String?,
        unlock: String,
        reason: String?
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
                    val intentCheckLock = Intent(context, CheckLockService::class.java)
                    context.startService(intentCheckLock)
                }
                if (accessibility) {
                    PreyLogger.d("lock 4")
                    PreyConfig.getInstance(context).setOverLock(false)
                    val intentAccessibility = Intent(context, PreyAccessibilityService::class.java)
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Thread.sleep(2000)
                PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
                    context,
                    "processed",
                    messageId,
                    UtilJson.makeMapParam("start", "lock", "started", reason)
                )
            } catch (e: Exception) {
                PreyLogger.e("Error sendNotifyAction: ${e.message}", e)
            }
        }
    }

    fun stop(
        context: Context,
        messageId: String?,
        reason: String
    ) {
        // Handle lock stop process based on device version
        if (PreyConfig.getInstance(context).isMarshmallowOrAbove()) {
            // Handle Marshmallow and above devices
            sendStopNotification(context, messageId, reason)
            handleAccessibilityAndOverlay(context)
        } else {
            // Handle pre-Marshmallow devices
            handlePreMarshmallow(context)
        }
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
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
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
            PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
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
        PreyConfig.getInstance(context).getWebServices().sendNotifyActionResultPreyHttp(
            context,
            "processed",
            messageId,
            UtilJson.makeMapParam("start", "lock", "stopped", reason)
        )
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
                    CoroutineScope(Dispatchers.IO).launch {
                        EventManagerRunner(
                            context,
                            Event(Event.NATIVE_LOCK)
                        )
                    }
                } else {
                    try {
                        FroyoSupport.getInstance(context).changePasswordAndLock(
                            PreyConfig.getInstance(context).getUnlockPass(),
                            true
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            EventManagerRunner(
                                context,
                                Event(Event.NATIVE_LOCK)
                            )
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("Error FroyoSupport changePasswordAndLock:${e.message}", e)
                    }
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