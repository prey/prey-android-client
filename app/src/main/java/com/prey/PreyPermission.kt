/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker

/**
 * Represents a PreyName entity, encapsulating code, name, and error information.
 */
object PreyPermission {

    const val ACCESS_BACKGROUND_LOCATION: String = "android.permission.ACCESS_BACKGROUND_LOCATION"

    /**
     * Checks if a permission is granted for the given context.
     *
     * @param context The context to check the permission for.
     * @param permission The permission to check.
     * @return True if the permission is granted, false otherwise.
     */
    fun canPermissionGranted(context: Context?, permission: String?): Boolean {
        val canPermissionGranted = (PermissionChecker.checkSelfPermission(
            context!!, permission!!
        ) ==
                PermissionChecker.PERMISSION_GRANTED)
        return canPermissionGranted
    }

    /**
     * Checks if a permission should be requested from the user.
     *
     * @param activity The activity to request the permission from.
     * @param permission The permission to request.
     * @return True if the permission should be requested, false otherwise.
     */
    fun shouldShowRequestPermission(activity: Activity, permission: String): Boolean {
        var shouldShowRequestPermission = false
        val isFirst =
            PreyConfig.getInstance(activity.applicationContext).getPermission(permission, true)
        PreyConfig.getInstance(activity.applicationContext).setPermission(permission, false)
        shouldShowRequestPermission = if (isFirst) {
            true
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission!!
            )
        }
        return shouldShowRequestPermission
    }

    fun canAccessFineLocation(context: Context?): Boolean {
        return canPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun showRequestFineLocation(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun canAccessCoarseLocation(context: Context?): Boolean {
        return canPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun showRequestCoarseLocation(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun canAccessBackgroundLocation(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT < PreyConfig.BUILD_VERSION_CODES_10) {
            true
        } else {
            canPermissionGranted(context, ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canAccessCamera(context: Context?): Boolean {
        return canPermissionGranted(context, Manifest.permission.CAMERA)
    }

    fun showRequestCamera(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.CAMERA)
    }

    fun showRequestPhone(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.READ_PHONE_STATE)
    }

    fun canAccessStorage(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return canPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            val image = canPermissionGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
            val audio = canPermissionGranted(context, Manifest.permission.READ_MEDIA_AUDIO)
            val video = canPermissionGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
            return image && audio && video
        }
    }

    fun showRequestStorage(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun showRequestBackgroundLocation(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT < PreyConfig.BUILD_VERSION_CODES_10) {
            true
        } else {
            shouldShowRequestPermission(activity, ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        if (PreyConfig.getInstance(context).isChromebook()) {
            return true
        }
        var canDrawOverlays = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canDrawOverlays = Settings.canDrawOverlays(context)
        }
        return canDrawOverlays
    }

    fun checkBiometricSupport(context: Context?): Boolean {
        return BiometricManager.from(context!!)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Method to obtain if accessibility service is enabled
     * @param context
     * @return true if accessibility service enabled, false otherwise
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val settingValue = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue == null) return false
        return settingValue.indexOf("prey") > 0
    }

    /**
     * Method that validates if the accessibility method should request it
     *
     * @param context context
     * @return true if accessibility method should request it, false otherwise
     */
    fun isAccessibilityServiceView(context: Context): Boolean {
        val isThereBatchInstallationKey = PreyBatch.getInstance(context).isThereBatchInstallationKey()
        //If it is batch, do not request accessibility
        if (isThereBatchInstallationKey) {
            return isThereBatchInstallationKey
        }
        val isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(context)
        PreyLogger.d(
            
                "isAccessibilityServiceEnabled:${isAccessibilityServiceEnabled}"
        )
        if (isAccessibilityServiceEnabled) {
            return isAccessibilityServiceEnabled
        } else {
            val accessibilityDenied = PreyConfig.getInstance(context).getAccessibilityDenied()
            PreyLogger.d("accessibilityDenied:${accessibilityDenied}")
            if (accessibilityDenied) {
                return accessibilityDenied
            } else {
                val isTimeNextAccessibility = PreyConfig.getInstance(context).isTimeNextAccessibility()
                PreyLogger.d("isTimeNextAccessibility:${isTimeNextAccessibility}")
                return isTimeNextAccessibility
            }
        }
    }

    /**
     * Method to obtain if storage is enabled
     * @param context context
     * @return true if storage enabled, false otherwise
     */
    fun isExternalStorageManager(context: Context?): Boolean {
        return true
    }

    /**
     * Method that validates if the storage method should request it
     *
     * @param context context
     * @return true if storage method should request it, false otherwise
     */
    fun isExternalStorageManagerView(context: Context?): Boolean {
        return true
    }

    /**
     * Method that validates if the background location method should request it
     *
     * @param context context
     * @return true if background location method should request it, false otherwise
     */
    fun canAccessBackgroundLocationView(context: Context): Boolean {
        val canAccessBackgroundLocation = canAccessBackgroundLocation(context)
        PreyLogger.d("canAccessBackgroundLocation:${canAccessBackgroundLocation}")
        if (canAccessBackgroundLocation) {
            return canAccessBackgroundLocation
        } else {
            val locatinBgDenied = PreyConfig.getInstance(context).getLocationBgDenied()
            PreyLogger.d("locatinBgDenied:${locatinBgDenied}")
            if (locatinBgDenied) {
                return locatinBgDenied
            } else {
                val isTimeNextLocationBg = PreyConfig.getInstance(context).isTimeNextLocationBg()
                PreyLogger.d("isTimeNextLocationBg:${isTimeNextLocationBg}")
                return isTimeNextLocationBg
            }
        }
    }

    fun areNotificationsEnabled(context: Context?): Boolean {
        return NotificationManagerCompat.from(context!!).areNotificationsEnabled()
    }

    /**
     * Method that validates whether exact alarms can be programmed
     *
     * @param context context
     * @return true if the caller can schedule exact alarms, false otherwise.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmMgr.canScheduleExactAlarms()
        } else {
            return true
        }
    }
}