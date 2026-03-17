/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location.daily

import android.content.Context
import com.prey.PreyLogger
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object that manages the persistent storage of the last time a daily action was performed.
 *
 * This class uses [android.content.SharedPreferences] to store a date string (formatted as yyyy-MM-dd)
 */
object DailyStore {

    private const val PREFS = "daily_location"
    private const val LAST_SENT_KEY = "last_sent_key"
    private const val DATE_FORMAT = "yyyy-MM-dd"

    /**
     * Checks if a daily location report has already been recorded as sent for the current day.
     *
     * @param context The application context to access SharedPreferences.
     * @return `true` if the last sent date stored matches today's date, `false` otherwise.
     */
    fun wasSentToday(context: Context): Boolean {
        val savedDay = getPrefs(context).getString(LAST_SENT_KEY, "")
        val today = getFormattedDate(Date())
        return savedDay == today
    }

    /**
     * Marks the current date as sent in the shared preferences.
     * This is used to track whether the daily location report has already been processed for today.
     *
     * @param context The context used to access shared preferences.
     */
    fun markSent(context: Context) {
        val today = getFormattedDate(Date())
        PreyLogger.i("markSent: $today")
        getPrefs(context).edit {
            putString(LAST_SENT_KEY, today)
        }
    }

    /**
     * Removes the stored record of the last day a report was sent.
     * This resets the daily status, causing [wasSentToday] to return false.
     *
     * @param context The application context used to access SharedPreferences.
     */
    fun removeSent(context: Context) {
        PreyLogger.d("removeSent")
        getPrefs(context).edit {
            remove(LAST_SENT_KEY)
        }
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun getFormattedDate(date: Date): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.US).format(date)
    }

}