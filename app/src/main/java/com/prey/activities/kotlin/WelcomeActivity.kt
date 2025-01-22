/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.FragmentActivity
import com.prey.backwardcompatibility.kotlin.FroyoSupport
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class WelcomeActivity : FragmentActivity() {
    public override fun onResume() {
        PreyLogger.d("onResume of WelcomeActivity")
        super.onResume()
        menu()
    }

    public override fun onPause() {
        PreyLogger.d("onPause of WelcomeActivity")
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        PreyLogger.d("onCreate of WelcomeActivity")
        menu()
    }

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
            startActivity(intent)
            finish()
        }
    }

    fun ready() {
        PreyLogger.d("ready WelcomeActivity")
        var intent: Intent? = null
        intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(applicationContext, CheckPasswordHtmlActivity::class.java)
        } else {
            Intent(applicationContext, DeviceReadyActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PreyLogger.d("requestCode:$requestCode resultCode:$resultCode")
        if (requestCode == SECURITY_PRIVILEGES) {
            menu()
            PreyConfig.getInstance(applicationContext).setProtectPrivileges(true)
        }
    }

    fun addPrivileges() {
        val intent = FroyoSupport.getInstance(applicationContext)!!.askForAdminPrivilegesIntent
        startActivityForResult(intent, SECURITY_PRIVILEGES)
    }

    companion object {
        private const val SECURITY_PRIVILEGES = 10
    }
}