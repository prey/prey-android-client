/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.os.Bundle
import com.prey.PreyConfig

/**
 * Activity that immediately closes itself when created.
 *
 * This activity is used to handle cases where the app needs to close itself
 * immediately, without performing any additional actions.
 */
class CloseActivity : Activity() {

    /**
     * Called when the activity is created.
     *
     * This method is responsible for initializing the activity's state and
     * finishing the activity immediately.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_CLOSE)
        finish()
    }

    companion object {
        const val ACTIVITY_CLOSE: String = "ACTIVITY_CLOSE"
    }

}