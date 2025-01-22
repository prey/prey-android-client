/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services.kotlin

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.prey.activities.kotlin.PasswordHtmlActivity
import com.prey.activities.kotlin.PasswordNativeActivity
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class AppAccessibilityService : AccessibilityService() {
    override fun onCreate() {
        super.onCreate()
        PreyLogger.d("AppAccessibilityService onCreate")
    }

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
            PreyLogger.e("Error onAccessibilityEvent:" + e.message, e)
        }
    }

    override fun onInterrupt() {
    }
}