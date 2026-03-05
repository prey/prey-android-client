/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import com.prey.PreyLogger
import java.util.Calendar
import androidx.core.content.edit

/**
 * Manages the persistence of the last execution time for daily location reports.
 * This object uses [android.content.SharedPreferences] to track whether an action
 * has already been performed during the current calendar day.
 */
object DailyStore{

    private const val PREFS = "daily_location"
    private const val LAST_SENT = "last_sent"

    /**
     * Checks if the action was already performed during the current calendar day.
     *
     * This method compares the timestamp of the last execution stored in [android.content.SharedPreferences]
     * against the start of the current day (00:00:00).
     *
     * @param context The application context used to access shared preferences.
     * @return `true` if the action was recorded today, `false` otherwise.
     */
    fun wasSentToday(context: Context): Boolean {
        val prefs =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastSentMillis = prefs.getLong(LAST_SENT, 0L)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStartMillis = calendar.timeInMillis
        val wasSentToday = lastSentMillis >= todayStartMillis
        PreyLogger.d("wasSentToday: $wasSentToday")
        return wasSentToday
    }

    /**
     * Records the current timestamp as the last time a report was successfully sent.
     * This updates the persistent storage to ensure that [wasSentToday] returns true
     * for the remainder of the current day.
     *
     * @param context The application context used to access SharedPreferences.
     */
    fun markSent(context: Context) {
        PreyLogger.d("markSent")
        val prefs =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(LAST_SENT, System.currentTimeMillis())
        }
    }

    /**
     * Removes the timestamp of the last sent execution from the shared preferences.
     * This effectively resets the daily status, causing [wasSentToday] to return false.
     *
     * @param context The application context to access SharedPreferences.
     */
    fun removeSent(context: Context) {
        PreyLogger.d("removeSent")
        val prefs =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            remove(LAST_SENT)
        }
    }

}