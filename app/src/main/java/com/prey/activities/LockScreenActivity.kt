/*******************************************************************************
 * Created by Prey Inc.
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.R
import com.prey.activities.js.CustomWebView
import com.prey.activities.js.WebAppInterface
import com.prey.receivers.PreyDeviceAdmin

class LockScreenActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_lock_screen)

        val unlockPass = PreyConfig.getPreyConfig(this).unlockPass
        if (unlockPass.isNullOrEmpty()) {
            finish()
            return
        }

        setupWebView()
        startLockTaskIfPossible()
    }

    private fun setupWebView() {
        webView = findViewById(R.id.lock_webview)
        webView.setOnKeyListener { _, _, keyEvent ->
            CustomWebView.callDispatchKeyEvent(applicationContext, keyEvent)
            false
        }

        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
        }
        webView.setBackgroundColor(0x00000000)

        val lng = PreyUtils.getLanguage()
        val lockMessage = PreyConfig.getPreyConfig(this).lockMessage
        val route = if (!lockMessage.isNullOrEmpty()) "lockmessage" else "lock"
        val url = "${CheckPasswordHtmlActivity.URL_ONB}#/$lng/$route"

        webView.addJavascriptInterface(
            WebAppInterface(this, this),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        webView.loadUrl(url)
    }

    private fun startLockTaskIfPossible() {
        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val adminComponent = ComponentName(this, PreyDeviceAdmin::class.java)

            if (dpm != null && dpm.isDeviceOwnerApp(packageName)) {
                dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
                startLockTask()
                PreyLogger.d("LockScreenActivity: startLockTask (Device Owner)")
            } else {
                startLockTask()
                PreyLogger.d("LockScreenActivity: startLockTask (standard)")
            }
        } catch (e: Exception) {
            PreyLogger.e("LockScreenActivity: startLockTask failed: ${e.message}", e)
        }
    }

    /**
     * Called by WebAppInterface after successful password verification.
     * Must stop lock task before finishing the activity.
     */
    fun unlockAndFinish() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null && am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                stopLockTask()
            }
        } catch (e: Exception) {
            PreyLogger.e("LockScreenActivity: stopLockTask failed: ${e.message}", e)
        }
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        val unlockPass = PreyConfig.getPreyConfig(this).unlockPass
        if (unlockPass.isNullOrEmpty()) {
            try { stopLockTask() } catch (_: Exception) {}
            finishAffinity()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back button while locked
    }
}
