/*******************************************************************************
 * Created by Prey Inc.
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.activities.PasswordHtmlActivity

class SecurityAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val unlockPass = PreyConfig.getPreyConfig(applicationContext).unlockPass
            if (unlockPass.isNullOrEmpty()) return

            val packageName = event?.packageName?.toString() ?: return

            if (!packageName.startsWith("com.prey") && !isInputMethod(packageName)) {
                // Dismiss any system dialog
                performGlobalAction(GLOBAL_ACTION_BACK)

                // Relaunch lock screen
                val intent = Intent(applicationContext, PasswordHtmlActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                applicationContext.startActivity(intent)

                // Follow-up dismissals to close status bar faster
                handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_BACK) }, 50)
                handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_BACK) }, 150)
            }
        } catch (e: Exception) {
            PreyLogger.e("SecurityAccessibilityService error: ${e.message}", e)
        }
    }

    private fun isInputMethod(packageName: String): Boolean {
        return try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.enabledInputMethodList.any { it.packageName == packageName }
        } catch (e: Exception) {
            false
        }
    }

    override fun onInterrupt() {}
}
