/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Window
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.json.actions.Lock
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.services.CheckLockActivated
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService

class LoginActivity : Activity() {
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        startup()
    }

    override fun onStart() {
        super.onStart()
        startup()
    }

    override fun onResume() {
        super.onResume()
        startup()
    }

    override fun onRestart() {
        super.onRestart()
        startup()
    }

    private fun startup() {
        var intentLock: Intent? = null
        val unlockPass = PreyConfig.getInstance(applicationContext).getUnlockPass()
        if (unlockPass != null && "" != unlockPass) {
            val canDrawOverlays = PreyPermission.canDrawOverlays(applicationContext)
            val accessibility = PreyPermission.isAccessibilityServiceEnabled(
                applicationContext
            )
            if (PreyConfig.getInstance(applicationContext).isMarshmallowOrAbove() &&
                (canDrawOverlays || accessibility)
            ) {
                PreyLogger.d("Login Boot finished. PreyLockService")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PreyLogger.d("login 2")
                    intentLock = Intent(applicationContext, PreyLockHtmlService::class.java)
                } else {
                    PreyLogger.d("login 3")
                    intentLock = Intent(applicationContext, PreyLockService::class.java)
                }
                applicationContext.startService(intentLock)
                applicationContext.startService(
                    Intent(
                        applicationContext,
                        CheckLockActivated::class.java
                    )
                )
            } else {
                Lock.lockWhenYouNocantDrawOverlays(applicationContext)
            }
        }
        val ready: Boolean = PreyConfig.getInstance(this).getProtectReady()
        if (isThereBatchInstallationKey && !ready) {
            showLoginBatch()
        } else {
            showLogin()
        }
    }

    private fun showLogin() {
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = Intent(this@LoginActivity, CheckPasswordHtmlActivity::class.java)
        } else {
            val registered = PreyConfig.getInstance(this).isThisDeviceAlreadyRegisteredWithPrey()
            if (registered) {
                intent = Intent(this@LoginActivity, CheckPasswordActivity::class.java)
            } else {
                val canDrawOverlays = PreyPermission.canDrawOverlays(this)
                PreyLogger.d(String.format("LoginActivity: canDrawOverlays:%b", canDrawOverlays))
                val isAdminActive = FroyoSupport.getInstance(this)!!.isAdminActive
                PreyLogger.d(String.format("LoginActivity: isAdminActive:%b", isAdminActive))
                val configurated = canDrawOverlays && isAdminActive
                intent = if (configurated) {
                    Intent(this@LoginActivity, SignInActivity::class.java)
                } else {
                    Intent(this@LoginActivity, OnboardingActivity::class.java)
                }
            }
        }
        if (PreyConfig.getInstance(this).isChromebook()) {
            intent = Intent(this@LoginActivity, ChromeActivity::class.java)
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showLoginBatch() {
        var intent: Intent? = null
        intent = Intent(this@LoginActivity, SplashBatchActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val isThisDeviceAlreadyRegisteredWithPrey: Boolean
        get() = PreyConfig.getInstance(this@LoginActivity)
            .isThisDeviceAlreadyRegisteredWithPrey(false)

    private fun showFeedback(ctx: Context) {
        val popup = Intent(ctx, FeedbackActivity::class.java)
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(popup)
    }

    private val isThereBatchInstallationKey: Boolean
        get() {
            val apiKeyBatch = PreyConfig.getInstance(this@LoginActivity).getApiKeyBatch()
            return (apiKeyBatch != null && "" != apiKeyBatch)
        }
}