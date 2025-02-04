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

class SetupActivity : PreyActivity() {
    protected fun getDeviceType(ctx: Context): String {
        return PreyUtils.getDeviceType(ctx)
    }

    override fun onBackPressed() {
        val intent = Intent(this@SetupActivity, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}