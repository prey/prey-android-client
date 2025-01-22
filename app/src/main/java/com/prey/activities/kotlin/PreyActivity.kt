/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.app.Activity
import android.os.Bundle
import android.view.Window
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyUtils

open class PreyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    protected val preyConfig: PreyConfig
        get() = PreyConfig.getInstance(this@PreyActivity)

    protected val deviceType: String
        get() = PreyUtils.getDeviceType(this)
}