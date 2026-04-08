/*******************************************************************************
 * Created by Prey Inc.
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.activities.LockScreenActivity

class SecurityAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val unlockPass = PreyConfig.getPreyConfig(applicationContext).unlockPass
            if (unlockPass.isNullOrEmpty()) return

            val packageName = event?.packageName?.toString() ?: return

            if (!packageName.startsWith("com.prey")) {
                PreyLogger.d("SecurityAccessibilityService: blocked $packageName, relaunching lock")
                val intent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                applicationContext.startActivity(intent)
            }
        } catch (e: Exception) {
            PreyLogger.e("SecurityAccessibilityService error: ${e.message}", e)
        }
    }

    override fun onInterrupt() {}
}
