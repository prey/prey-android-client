/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

import com.prey.activities.PasswordHtmlActivity
import com.prey.activities.PasswordNativeActivity
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * AppAccessibilityService is an AccessibilityService that handles accessibility events.
 * It checks if the device is locked and if so, it starts the password activity.
 */
class AppAccessibilityService : AccessibilityService() {

    /**
     * Called when the service is created.
     */
    override fun onCreate() {
        super.onCreate()
        PreyLogger.d("AppAccessibilityService onCreate")
    }

    /**
     * Called when an accessibility event occurs.
     *
     * @param accessibilityEvent The accessibility event that occurred.
     */
    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        try {
            val unlockPass = PreyConfig.getInstance(applicationContext).getUnlockPass()
            val isLock = unlockPass != null && "" != unlockPass
            if (isLock) {
                PreyLogger.d("acc 1")
                if (accessibilityEvent?.packageName != null) {
                    val charSequence =
                        if (accessibilityEvent.packageName != null) accessibilityEvent.packageName.toString() else null
                    if ("com.prey" == charSequence || "android" == charSequence) {
                    } else {
                        if (isLock) {
                            PreyLogger.d("acc 2")
                            var intentPasswordActivity: Intent? = null
                            intentPasswordActivity =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Intent(applicationContext, PasswordHtmlActivity::class.java)
                                } else {
                                    Intent(
                                        applicationContext,
                                        PasswordNativeActivity::class.java
                                    )
                                }
                            intentPasswordActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            applicationContext.startActivity(intentPasswordActivity)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error onAccessibilityEvent:${e.message}" , e)
        }
    }

    /**
     * Called when the service is interrupted.
     */
    override fun onInterrupt() {
    }
}