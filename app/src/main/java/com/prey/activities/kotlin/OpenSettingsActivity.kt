/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import com.prey.R
import com.prey.events.factories.kotlin.EventFactory

class OpenSettingsActivity : Activity() {
    /**
     * Activity that checks if it should hide the notification or
     * should open the settings to grant permissions
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.splash_batch)
        val verifyNotification = EventFactory.verifyNotification(this)
        if (verifyNotification) {
            val manager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(EventFactory.NOTIFICATION_ID)
        } else {
            val intentSetting = Intent()
            intentSetting.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", this.packageName, null)
            intentSetting.setData(uri)
            intentSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            this.startActivity(intentSetting)
        }
        finish()
    }
}
