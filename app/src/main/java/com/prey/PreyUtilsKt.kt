package com.prey

import android.content.Context

/**
 * Utility object containing extension functions and helper methods for the Prey application.
 */
object PreyUtilsKt {

    /**
     * Determines if the current device is a tablet based on its smallest screen width.
     *
     * A device is considered a tablet if its smallest screen width (sw) is at least 600dp.
     *
     * @return true if the device is a tablet, false otherwise.
     */
    fun Context.isTablet(): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }
}