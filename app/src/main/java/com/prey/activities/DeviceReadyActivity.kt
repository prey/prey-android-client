/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback

import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * DeviceReadyActivity is the main activity of the application.
 * It handles the device ready state and provides options to navigate to PanelWebActivity or PreyConfigurationActivity.
 */
class DeviceReadyActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {

    /**
     * Called when the user presses the back button.
     * Starts CheckPasswordHtmlActivity and finishes the current activity.
     */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        startActivity(Intent(application, CheckPasswordHtmlActivity::class.java))
        finish()
    }

    /**
     * Called when the activity is resumed.
     * Cancels the notification with tag PreyConfig.TAG and id PreyConfig.NOTIFY_ANDROID_6.
     */
    override fun onResume() {
        super.onResume()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
            PreyConfig.TAG,
            PreyConfig.NOTIFY_ANDROID_6
        )
    }

    /**
     * Called when the activity is created.
     * Sets up the activity layout, fonts, and click listeners.
     *
     * @param savedInstanceState The saved instance state, or null if not saved
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.device_ready)
        PreyLogger.d("onCreate of DeviceReadyActivity")
        val boldFont = Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Bold.ttf")
        val regularFont =
            Typeface.createFromAsset(assets, "fonts/MagdaClean/magdacleanmono-regular.ttf")
        findViewById<TextView>(R.id.textView3_1).typeface = regularFont
        findViewById<TextView>(R.id.textView3_2).typeface = boldFont
        findViewById<TextView>(R.id.textView4_1).typeface = regularFont
        findViewById<TextView>(R.id.textView4_2).typeface = boldFont
        findViewById<LinearLayout>(R.id.linearLayout1).setOnClickListener {
            startActivity(Intent(application, PanelWebActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.linearLayout2).setOnClickListener {
            startActivity(Intent(application, PreyConfigurationActivity::class.java))
            finish()
        }
    }
}