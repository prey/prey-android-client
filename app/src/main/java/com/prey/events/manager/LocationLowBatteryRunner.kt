package com.prey.events.manager

import android.content.Context
import com.prey.PreyConfig
import com.prey.PreyLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Singleton object responsible for managing and throttling "Low Battery" location events.
 *
 * This runner ensures that location-based low battery events are not triggered too frequently,
 * implementing a cooldown period (3 hours) between consecutive valid events to optimize
 * battery usage and prevent redundant notifications.
 */
object LocationLowBatteryRunner {

    private const val THREE_HOURS_MS = 3 * 60 * 60 * 1000L
    private const val DATE_FORMAT = "dd/MM/yy HH:mm:ss"

    /**
     * Determines if a low battery location event is valid to be processed.
     *
     * This checks if a minimum time interval ([THREE_HOURS_MS]) has elapsed since the
     * last recorded event to prevent redundant triggers. If the event is valid, the
     * last event timestamp is updated to the current time.
     *
     */
    fun isValid(ctx: Context): Boolean {
        return try {
            val config = PreyConfig.getPreyConfig(ctx)
            val lastBatteryEventDate = config.locationLowBatteryDate
            val currentTime = System.currentTimeMillis()
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            PreyLogger.d("EVENT lastEvent: $lastBatteryEventDate ${sdf.format(Date(lastBatteryEventDate))}")
            PreyLogger.d("EVENT now:       $currentTime ${sdf.format(Date(currentTime))}")
            PreyLogger.d("EVENT diff:      ${currentTime - lastBatteryEventDate}")
            if (lastBatteryEventDate == 0L || (currentTime - lastBatteryEventDate) > THREE_HOURS_MS) {
                config.locationLowBatteryDate = currentTime
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

}