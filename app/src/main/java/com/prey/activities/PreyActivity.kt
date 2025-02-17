/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.os.Bundle
import android.view.Window

import com.prey.PreyConfig
import com.prey.PreyUtils

/**
 * Base activity class for Prey activities.
 * This class provides common functionality for all Prey activities.
 */
open class PreyActivity : Activity() {

    /**
     * Called when the activity is created.
     * Initializes the activity and sets up the window feature.
     *
     * @param savedInstanceState The saved instance state, or null if not saved
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    /**
     * Returns the Prey configuration instance for this activity.
     * The Prey configuration instance provides access to Prey settings and preferences.
     *
     * @return The Prey configuration instance
     */
    protected fun getPreyConfig(): PreyConfig = PreyConfig.getInstance(this@PreyActivity)

    /**
     * Returns the type of device this activity is running on.
     * The device type is determined by the PreyUtils class.
     *
     * @return The device type (e.g. "Laptop", "Tablet", etc.)
     */
    protected fun getDeviceType(): String = PreyUtils.getDeviceType(this)

}