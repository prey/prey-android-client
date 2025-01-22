/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.WindowManager
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.activities.kotlin.CloseActivity
import com.prey.activities.kotlin.PasswordHtmlActivity
import com.prey.activities.kotlin.PasswordNativeActivity
import com.prey.backwardcompatibility.kotlin.FroyoSupport
import com.prey.events.kotlin.Event
import com.prey.events.manager.kotlin.EventManagerRunner
import com.prey.exceptions.kotlin.PreyException
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.kotlin.PreyPermission
import com.prey.net.kotlin.PreyWebServices
import com.prey.services.kotlin.AppAccessibilityService
import com.prey.services.kotlin.CheckLockActivated
import com.prey.services.kotlin.PreyLockHtmlService
import com.prey.services.kotlin.PreyLockService
import org.json.JSONObject

class Lock  {

    fun start(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        try {
            var messageId: String? = null
            if (parameters != null && parameters.has(PreyConfig.MESSAGE_ID)) {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID)
                PreyLogger.d("messageId:$messageId")
            }
            var reason: String? = null
            var jobId: String? = null
            if (parameters != null && parameters.has(PreyConfig.JOB_ID)) {
                jobId = parameters.getString(PreyConfig.JOB_ID)
                PreyLogger.d("jobId:$jobId")
                reason = "{\"device_job_id\":\"$jobId\"}"
                PreyConfig.getInstance(ctx).setJobIdLock (jobId)
            }
            var unlock: String? = null
            if (parameters != null && parameters.has(PreyConfig.UNLOCK_PASS)) {
                unlock = parameters.getString(PreyConfig.UNLOCK_PASS)
                PreyConfig.getInstance(ctx).setUnlockPass  (unlock)
            }
            if (parameters != null && parameters.has(PreyConfig.LOCK_MESSAGE)) {
                val lockMessage = parameters.getString(PreyConfig.LOCK_MESSAGE)
                PreyConfig.getInstance(ctx).setLockMessage  (lockMessage)
            } else {
                PreyConfig.getInstance(ctx).setLockMessage ("")
            }
            lock(ctx, unlock!!, messageId, reason, jobId)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "lock", "failed", e.message)
            )
        }
    }

    fun stop(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        try {
            var messageId: String? = null
            if (parameters != null && parameters.has(PreyConfig.MESSAGE_ID)) {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID)
                PreyLogger.d("messageId:$messageId")
            }
            var reason = "{\"origin\":\"panel\"}"

            if (parameters != null && parameters.has(PreyConfig.JOB_ID)) {
                val jobId = parameters.getString(PreyConfig.JOB_ID)
                PreyLogger.d("jobId:$jobId")
                reason = "{\"device_job_id\":\"$jobId\",\"origin\":\"panel\"}"
            }
            val jobIdLock = PreyConfig.getInstance(ctx).getJobIdLock()
            if (jobIdLock != null && "" != jobIdLock) {
                reason = "{\"device_job_id\":\"$jobIdLock\",\"origin\":\"panel\"}"
                PreyConfig.getInstance(ctx).setJobIdLock ("")
            }
            PreyConfig.getInstance(ctx).setLockMessage ("")
            PreyConfig.getInstance(ctx).setLock(false)
            PreyConfig.getInstance(ctx).deleteUnlockPass()
            if (PreyConfig.getInstance(ctx).isMarshmallowOrAbove()) {
                Thread.sleep(1000)
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    ctx,
                    "processed",
                    messageId,
                    UtilJson.makeMapParam("start", "lock", "stopped", reason)
                )
                Thread.sleep(2000)
                val canAccessibility = PreyPermission.isAccessibilityServiceEnabled(ctx)
                val canDrawOverlays = PreyPermission.canDrawOverlays(ctx)
                if (canDrawOverlays || canAccessibility) {
                    if (canDrawOverlays) {
                        try {
                            val view = PreyConfig.getInstance(ctx).viewLock
                            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            if (wm != null && view != null) {
                                wm.removeView(view)
                                PreyConfig.getInstance(ctx).viewLock = null
                            } else {
                                Process.killProcess(Process.myPid())
                            }
                        } catch (e: Exception) {
                            Process.killProcess(Process.myPid())
                        }
                    }
                    val intentClose = Intent(ctx, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intentClose)
                } else {
                    try {
                        FroyoSupport.getInstance(ctx)!!.changePasswordAndLock("", true)
                        val screenLock =
                            (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                PreyConfig.TAG
                            )
                        screenLock.acquire()
                        screenLock.release()
                        Thread.sleep(2000)
                        reason = "{\"origin\":\"panel\"}"
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                            ctx,
                            UtilJson.makeMapParam("start", "lock", "stopped", reason)
                        )
                    } catch (e: Exception) {
                        throw PreyException(e)
                    }
                }
            } else {
                try {
                    if (!PreyConfig.getInstance(ctx).isMarshmallowOrAbove()) {
                        FroyoSupport.getInstance(ctx)!!.changePasswordAndLock("", true)
                        val screenLock =
                            (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                PreyConfig.TAG
                            )
                        screenLock.acquire()
                        screenLock.release()
                    }
                    Thread.sleep(2000)
                    reason = "{\"origin\":\"panel\"}"
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                        ctx,
                        UtilJson.makeMapParam("start", "lock", "stopped", reason)
                    )
                } catch (e: Exception) {
                    throw PreyException(e)
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "lock", "failed", e.message)
            )
        }
    }

    fun sms(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject) {
        try {
            val unlock = parameters.getString("parameter")
            lock(ctx, unlock, null, null, null)
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx,
                UtilJson.makeMapParam("start", "lock", "failed", e.message)
            )
        }
    }

    fun lock(
        ctx: Context,
        unlock: String,
        messageId: String?,
        reason: String?,
        device_job_id: String?
    ) {
        PreyLogger.d(
            String.format(
                "lock unlock:%s messageId:%s reason:%s",
                unlock,
                messageId,
                reason
            )
        )
        PreyConfig.getInstance(ctx).setUnlockPass (unlock )
        PreyConfig.getInstance(ctx).setLock(true)
        PreyLogger.d("lock 1")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val accessibility = PreyPermission.isAccessibilityServiceEnabled(ctx)
            val canDrawOverlays = PreyPermission.canDrawOverlays(ctx)
            if (canDrawOverlays || accessibility) {
                if (canDrawOverlays) {
                    var intentPreyLock: Intent? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PreyLogger.d("lock 2")
                        intentPreyLock = Intent(ctx, PreyLockHtmlService::class.java)
                    } else {
                        PreyLogger.d("lock 3")
                        intentPreyLock = Intent(ctx, PreyLockService::class.java)
                    }
                    ctx.startService(intentPreyLock)
                    val intentCheckLock = Intent(ctx, CheckLockActivated::class.java)
                    ctx.startService(intentCheckLock)
                }
                if (accessibility) {
                    PreyLogger.d("lock 4")
                    PreyConfig.getInstance(ctx).setOverLock( false)
                    val intentAccessibility = Intent(ctx, AppAccessibilityService::class.java)
                    ctx.startService(intentAccessibility)
                    var intentPasswordActivity: Intent? = null
                    intentPasswordActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Intent(ctx, PasswordHtmlActivity::class.java)
                    } else {
                        Intent(ctx, PasswordNativeActivity::class.java)
                    }
                    intentPasswordActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intentPasswordActivity)
                }
            } else {
                PreyLogger.d("lock 5")
                lockWhenYouNocantDrawOverlays(ctx)
            }
        } else {
            PreyLogger.d("lock 6")
            lockOld(ctx)
        }
        Thread {
            try {
                Thread.sleep(2000)
                PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                    ctx,
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
    private fun isDeviceLocked(ctx: Context): Boolean {
        val keyguardManager =
            ctx.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 23+
        return keyguardManager.isDeviceSecure
    }

    companion object {
        fun sendUnLock(context: Context) {
            Thread {
                val unlockPass = PreyConfig.getInstance(context).getUnlockPass()
                PreyLogger.d("sendUnLock unlockPass:$unlockPass")
                if (unlockPass != null && "" != unlockPass) {
                    if (PreyConfig.getInstance(context).isMarshmallowOrAbove() && PreyPermission.canDrawOverlays(
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
                        val ctx = context
                        object : Thread() {
                            override fun run() {
                                val jobIdLock = PreyConfig.getInstance(ctx).getJobIdLock()
                                var reason = "{\"origin\":\"user\"}"
                                if (jobIdLock != null && "" != jobIdLock) {
                                    reason =
                                        "{\"origin\":\"user\",\"device_job_id\":\"$jobIdLock\"}"
                                    PreyConfig.getInstance(ctx).setJobIdLock ("")
                                }
                                PreyWebServices.getInstance()
                                    .sendNotifyActionResultPreyHttp(
                                        ctx,
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

        fun lockWhenYouNocantDrawOverlays(ctx: Context) {
            val accessibility = PreyPermission.isAccessibilityServiceEnabled(ctx)
            val canDrawOverlays = PreyPermission.canDrawOverlays(ctx)
            val unlockPass = PreyConfig.getInstance(ctx).getUnlockPass()
            PreyLogger.d(
                String.format(
                    "DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass: %s accessibility: %s canDrawOverlays: %s",
                    unlockPass,
                    accessibility,
                    canDrawOverlays
                )
            )
            val isAccessibilityServiceEnabled = PreyPermission.isAccessibilityServiceEnabled(ctx)
            if (unlockPass != null && "" != unlockPass) {
                if (!canDrawOverlays(ctx) && !isAccessibilityServiceEnabled) {
                    val isPatternSet = isPatternSet(ctx)
                    val isPassOrPinSet = isPassOrPinSet(ctx)
                    PreyLogger.d("CheckLockActivated isPatternSet:$isPatternSet")
                    PreyLogger.d("CheckLockActivated  isPassOrPinSet:$isPassOrPinSet")
                    if (isPatternSet || isPassOrPinSet) {
                        FroyoSupport.getInstance(ctx)!!.lockNow()
                        Thread(EventManagerRunner(ctx, Event(Event.NATIVE_LOCK))).start()
                    } else {
                        try {
                            FroyoSupport.getInstance(ctx)!!.changePasswordAndLock(
                                PreyConfig.getInstance(ctx).getUnlockPass(),
                                true
                            )
                            Thread(EventManagerRunner(ctx, Event(Event.NATIVE_LOCK))).start()
                        } catch (e: Exception) {
                            PreyLogger.e("Error FroyoSupport changePasswordAndLock:" + e.message, e)
                        }
                    }
                }
            }
        }

        fun lockOld(ctx: Context) {
            val accessibility = PreyPermission.isAccessibilityServiceEnabled(ctx)
            val canDrawOverlays = PreyPermission.canDrawOverlays(ctx)
            val unlockPass = PreyConfig.getInstance(ctx).getUnlockPass()
            PreyLogger.d("DeviceAdmin lockWhenYouNocantDrawOverlays unlockPass1:$unlockPass accessibility:$accessibility canDrawOverlays:$canDrawOverlays")
            if (unlockPass != null && "" != unlockPass) {
                val isPatternSet = isPatternSet(ctx)
                val isPassOrPinSet = isPassOrPinSet(ctx)
                PreyLogger.d("CheckLockActivated isPatternSet:$isPatternSet")
                PreyLogger.d("CheckLockActivated  isPassOrPinSet:$isPassOrPinSet")
                if (isPatternSet || isPassOrPinSet) {
                    FroyoSupport.getInstance(ctx)!!.lockNow()
                } else {
                    try {
                        FroyoSupport.getInstance(ctx)!!
                            .changePasswordAndLock(PreyConfig.getInstance(ctx).getUnlockPass(), true)
                    } catch (e: Exception) {
                        PreyLogger.e("error lockold:" + e.message, e)
                    }
                }
            }
        }

        fun canDrawOverlays(ctx: Context?): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }
            return Settings.canDrawOverlays(ctx)
        }

        /**
         * @return true if pattern set, false if not (or if an issue when checking)
         */
        fun isPatternSet(ctx: Context?): Boolean {
            return false
        }

        /**
         * @return true if pass or pin set
         */
        @TargetApi(16)
        fun isPassOrPinSet(ctx: Context): Boolean {
            val keyguardManager =
                ctx.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 16+
            return keyguardManager.isKeyguardSecure
        }
    }
}