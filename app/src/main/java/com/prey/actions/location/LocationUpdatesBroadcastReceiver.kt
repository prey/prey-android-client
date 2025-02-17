package com.prey.actions.location

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult

import com.prey.PreyLogger

/**
 * A BroadcastReceiver that listens for location updates and processes them accordingly.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PROCESS_UPDATES) {
            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.map { location ->
                    PreyLogger.d("___||___onReceive()  latitude = ${location.latitude}  ____longitude = ${location.longitude}")
                }
            }
        }
    }

    /**
     * Checks if the app is currently in the foreground.
     *
     * @param context The Context to use for the check.
     * @return True if the app is in the foreground, false otherwise.
     */
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        appProcesses.forEach { appProcess ->
            if (appProcess.importance ==
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == context.packageName
            ) {
                return true
            }
        }
        return false
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.prey.actions.location.LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES"
    }
}