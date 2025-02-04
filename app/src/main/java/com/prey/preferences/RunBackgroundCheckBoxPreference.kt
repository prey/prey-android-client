/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences

import android.content.Context
import android.content.Intent
import android.preference.CheckBoxPreference
import android.util.AttributeSet
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.services.PreyNotificationForeGroundService

class RunBackgroundCheckBoxPreference : CheckBoxPreference {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        PreyLogger.d("RunBackgroundCheckBoxPreference:$checked")
        val ctx = context
        if (checked) {
            notifyReady(ctx)
        } else {
            notifyCancel(ctx)
        }
        PreyConfig.getInstance(ctx).setRunBackground(checked)
    }

    companion object {
        fun notifyReady(ctx: Context?) {
        }

        fun notifyCancel(ctx: Context) {
            if (!PreyUtils.isChromebook(ctx)) {
                try {
                    ctx.stopService(Intent(ctx, PreyNotificationForeGroundService::class.java))
                } catch (e: Exception) {
                    PreyLogger.e("notifyCancel:" + e.message, e)
                }
            }
        }
    }
}