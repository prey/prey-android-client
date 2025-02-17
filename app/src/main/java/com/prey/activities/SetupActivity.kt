/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.content.Context
import android.content.Intent

import com.prey.PreyUtils

/**
 * SetupActivity is responsible for handling the setup process of the application.
 * It extends PreyActivity and provides functionality for getting the device type and handling the back press event.
 */
class SetupActivity : PreyActivity() {

    /**
     * Returns the device type based on the provided context.
     *
     * @param context The application context.
     * @return The device type as a string.
     */
    protected fun getDeviceType(context: Context): String {
        // Delegate the device type retrieval to PreyUtils
        return PreyUtils.getDeviceType(context)
    }

    /**
     * Handles the back press event by starting the WelcomeActivity and finishing the current activity.
     */
    override fun onBackPressed() {
        // Create an intent to start the WelcomeActivity
        val intent = Intent(this, WelcomeActivity::class.java)
        // Start the WelcomeActivity
        startActivity(intent)
        // Finish the current activity
        finish()
    }
}