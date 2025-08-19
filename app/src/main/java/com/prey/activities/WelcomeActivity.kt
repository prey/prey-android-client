/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.FragmentActivity

import com.prey.backwardcompatibility.FroyoSupport
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * WelcomeActivity is the initial activity of the application. It handles the
 * registration process and redirects the user to the LoginActivity if the
 * device is not registered.
 */
class WelcomeActivity : FragmentActivity() {

    /**
     * Called when the activity is resumed. It calls the menu function to
     * determine the next step in the registration process.
     */
    public override fun onResume() {
        PreyLogger.d("onResume of WelcomeActivity")
        super.onResume()
        menu()
    }

    /**
     * Called when the activity is paused.
     */
    public override fun onPause() {
        PreyLogger.d("onPause of WelcomeActivity")
        super.onPause()
    }

    /**
     * Called when the device configuration changes.
     *
     * @param newConfig The new device configuration.
     */

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * Called when the activity is created. It sets up the activity's UI and
     * calls the menu function to determine the next step in the registration
     * process.
     *
     * @param savedInstanceState The saved instance state of the activity, or
     * null if not applicable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        PreyLogger.d("onCreate of WelcomeActivity")
        menu()
    }

    /**
     * Determines the next step in the registration process based on the
     * device's registration status and email address.
     */
    fun menu() {
        PreyLogger.d("menu WelcomeActivity")
        val email: String? = PreyConfig.getInstance(this).getEmail()
        if (email == null || "" == email) {
            PreyLogger.d("email:$email")
            PreyConfig.getInstance(this).setProtectReady(false)
            PreyConfig.getInstance(this).setProtectAccount(false)
            PreyConfig.getInstance(this).setProtectTour(false)
        }
        if (PreyConfig.getInstance(this).isThisDeviceAlreadyRegisteredWithPrey()) {
            ready()
        } else {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            PreyConfig.getInstance(applicationContext).setActivityView(LOGIN_ACTIVITY)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Redirects the user to the ready activity if the device is registered.
     */
    fun ready() {
        PreyLogger.d("ready WelcomeActivity")
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyConfig.getInstance(applicationContext).setActivityView(CHECK_PASSWORD_ACTIVITY)
            intent = Intent(applicationContext, CheckPasswordHtmlActivity::class.java)
        } else {
            PreyConfig.getInstance(applicationContext).setActivityView(DEVICE_READY_ACTIVITY)
            intent = Intent(applicationContext, DeviceReadyActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Handles the result of the admin privileges request.
     *
     * @param requestCode The request code of the admin privileges request.
     * @param resultCode The result code of the admin privileges request.
     * @param data The intent data of the admin privileges request.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PreyLogger.d("requestCode:$requestCode resultCode:$resultCode")
        if (requestCode == SECURITY_PRIVILEGES) {
            menu()
            PreyConfig.getInstance(applicationContext).setProtectPrivileges(true)
        }
    }

    /**
     * Requests admin privileges for the device.
     */
    fun addPrivileges() {
        val intent = FroyoSupport.getInstance(applicationContext).getAskForAdminPrivilegesIntent()
        startActivityForResult(intent, SECURITY_PRIVILEGES)
    }

    companion object {
        private const val SECURITY_PRIVILEGES = 10
        const val CHECK_PASSWORD_ACTIVITY = "CheckPasswordHtmlActivity"
        const val DEVICE_READY_ACTIVITY = "DeviceReadyActivity"
        const val LOGIN_ACTIVITY = "LoginActivity"
    }

}