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
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.actions.lock.LockAction
import com.prey.services.CheckLockService
import com.prey.services.PreyLockHtmlService
import com.prey.services.PreyLockService

/**
 * LoginActivity is the main activity of the application.
 * It handles the login process and redirects the user to the
 * appropriate activity based on the device's registration status.
 */
class LoginActivity : Activity() {

    /**
     * Called when the configuration of the activity changes.
     *
     * @param newConfig The new configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState The saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        startup()
    }

    /**
     * Called when the activity is started.
     */
    override fun onStart() {
        super.onStart()
        startup()
    }

    /**
     * Called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        startup()
    }

    /**
     * Called when the activity is restarted.
     */
    override fun onRestart() {
        super.onRestart()
        startup()
    }

    /**
     * Starts the login process.
     *
     * This method checks if the device is registered, and if so,
     * starts the PreyLockService.
     *
     * If the device is not registered, it shows the login activity.
     *
     * If the device is registered, but the user has not unlocked
     * the device, it shows the lock activity.
     */
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
                        CheckLockService::class.java
                    )
                )
            } else {
                LockAction().lockWhenYouNocantDrawOverlays(applicationContext)
            }
        }
        val ready: Boolean = PreyConfig.getInstance(this).getProtectReady()
        if (isThereBatchInstallationKey() && !ready) {
            showLoginBatch()
        } else {
            showLogin()
        }
    }

    /**
     * Shows the login activity.
     */
    private fun showLogin() {
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PreyConfig.getInstance(applicationContext)
                .setActivityView(ACTIVITY_LOGIN_CHECK_PASSWORD_HTML)
            intent = Intent(this@LoginActivity, CheckPasswordHtmlActivity::class.java)
        } else {
            val registered = PreyConfig.getInstance(this).isThisDeviceAlreadyRegisteredWithPrey()
            if (registered) {
                PreyConfig.getInstance(applicationContext)
                    .setActivityView(ACTIVITY_LOGIN_CHECK_PASSWORD)
                intent = Intent(this@LoginActivity, CheckPasswordActivity::class.java)
            } else {
                val canDrawOverlays = PreyPermission.canDrawOverlays(this)
                PreyLogger.d("LoginActivity: canDrawOverlays:${canDrawOverlays}")
                val isAdminActive = FroyoSupport.getInstance(this).isAdminActive()
                PreyLogger.d("LoginActivity: isAdminActive:${isAdminActive}")
                val configurated = canDrawOverlays && isAdminActive
                intent = if (configurated) {
                    PreyConfig.getInstance(applicationContext)
                        .setActivityView(ACTIVITY_LOGIN_CHECK_SIGN_IN)
                    Intent(this@LoginActivity, SignInActivity::class.java)
                } else {
                    PreyConfig.getInstance(applicationContext)
                        .setActivityView(ACTIVITY_LOGIN_CHECK_ONBOARDING)
                    Intent(this@LoginActivity, OnboardingActivity::class.java)
                }
            }
        }
        if (PreyConfig.getInstance(this).isChromebook()) {
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_LOGIN_CHECK_CHROME)
            intent = Intent(this@LoginActivity, ChromeActivity::class.java)
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Shows the login batch activity.
     */
    private fun showLoginBatch() {
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_LOGIN_CHECK_SPLASH)
        var intent: Intent? = null
        intent = Intent(this@LoginActivity, SplashBatchActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isThisDeviceAlreadyRegisteredWithPrey(): Boolean =
        PreyConfig.getInstance(this@LoginActivity)
            .isThisDeviceAlreadyRegisteredWithPrey(false)

    private fun showFeedback(context: Context) {
        val popup = Intent(context, FeedbackActivity::class.java)
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(popup)
    }

    private fun isThereBatchInstallationKey(): Boolean {
        val apiKeyBatch = PreyConfig.getInstance(this@LoginActivity).getApiKeyBatch()
        return (apiKeyBatch != null && "" != apiKeyBatch)
    }

    companion object {
        const val ACTIVITY_LOGIN_CHECK_PASSWORD_HTML: String = "ACTIVITY_LOGIN_CHECK_PASSWORD_HTML"
        const val ACTIVITY_LOGIN_CHECK_PASSWORD: String = "ACTIVITY_LOGIN_CHECK_PASSWORD"
        const val ACTIVITY_LOGIN_CHECK_SIGN_IN: String = "ACTIVITY_LOGIN_CHECK_SIGN_IN"
        const val ACTIVITY_LOGIN_CHECK_ONBOARDING: String = "ACTIVITY_LOGIN_CHECK_ONBOARDING"
        const val ACTIVITY_LOGIN_CHECK_SPLASH: String = "ACTIVITY_LOGIN_CHECK_SPLASH"
        const val ACTIVITY_LOGIN_CHECK_CHROME: String = "ACTIVITY_LOGIN_CHECK_CHROME"
    }

}