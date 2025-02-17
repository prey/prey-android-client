/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.aware.AwareController

import java.util.Date

/**
 * This class is responsible for handling daily location updates.
 */
class DailyLocation {

    /**
     * Checks if a daily location update should be sent.
     *
     * This method determines whether a daily location update is due based on the current date and the last sent date.
     *
     * @param context The application context.
     */
    fun run(context: Context) {
        val dailyLocation = PreyConfig.getInstance(context).getDailyLocation()
        val nowDailyLocation = PreyConfig.FORMAT_SDF_AWARE.format(Date())
        if (nowDailyLocation != dailyLocation) {
            AwareController.getInstance().initDailyLocation(context)
        } else {
            PreyLogger.d("DAILY location already sent")
        }
    }

}