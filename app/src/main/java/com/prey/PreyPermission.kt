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

object PreyPermission {
    const val ACCESS_BACKGROUND_LOCATION: String = "android.permission.ACCESS_BACKGROUND_LOCATION"

    fun canPermissionGranted(ctx: Context?, permission: String?): Boolean {
        val canPermissionGranted = (PermissionChecker.checkSelfPermission(
            ctx!!, permission!!
        ) ==
                PermissionChecker.PERMISSION_GRANTED)
        return canPermissionGranted
    }

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

    fun canAccessFineLocation(ctx: Context?): Boolean {
        return canPermissionGranted(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun showRequestFineLocation(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun canAccessCoarseLocation(ctx: Context?): Boolean {
        return canPermissionGranted(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun showRequestCoarseLocation(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun canAccessBackgroundLocation(ctx: Context?): Boolean {
        return if (Build.VERSION.SDK_INT < PreyConfig.BUILD_VERSION_CODES_10) {
            true
        } else {
            canPermissionGranted(ctx, ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canAccessCamera(ctx: Context?): Boolean {
        return canPermissionGranted(ctx, Manifest.permission.CAMERA)
    }

    fun showRequestCamera(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.CAMERA)
    }

    fun showRequestPhone(activity: Activity): Boolean {
        return shouldShowRequestPermission(activity, Manifest.permission.READ_PHONE_STATE)
    }

    fun canAccessStorage(ctx: Context?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return canPermissionGranted(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            val image = canPermissionGranted(ctx, Manifest.permission.READ_MEDIA_IMAGES)
            val audio = canPermissionGranted(ctx, Manifest.permission.READ_MEDIA_AUDIO)
            val video = canPermissionGranted(ctx, Manifest.permission.READ_MEDIA_VIDEO)
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

    fun canDrawOverlays(ctx: Context): Boolean {
        if (PreyConfig.getInstance(ctx).isChromebook()) {
            return true
        }
        var canDrawOverlays = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canDrawOverlays = Settings.canDrawOverlays(ctx)
        }
        return canDrawOverlays
    }

    fun checkBiometricSupport(ctx: Context?): Boolean {
        return BiometricManager.from(ctx!!)
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
     * @param ctx context
     * @return true if accessibility method should request it, false otherwise
     */
    fun isAccessibilityServiceView(ctx: Context): Boolean {
        val isThereBatchInstallationKey = PreyBatch.getInstance(ctx)!!.isThereBatchInstallationKey
        //If it is batch, do not request accessibility
        if (isThereBatchInstallationKey) {
            return isThereBatchInstallationKey
        }
        val isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(ctx)
        PreyLogger.d(
            String.format(
                "isAccessibilityServiceEnabled:%s",
                isAccessibilityServiceEnabled
            )
        )
        if (isAccessibilityServiceEnabled) {
            return isAccessibilityServiceEnabled
        } else {
            val accessibilityDenied = PreyConfig.getInstance(ctx).getAccessibilityDenied()
            PreyLogger.d(String.format("accessibilityDenied:%s", accessibilityDenied))
            if (accessibilityDenied) {
                return accessibilityDenied
            } else {
                val isTimeNextAccessibility = PreyConfig.getInstance(ctx).isTimeNextAccessibility()
                PreyLogger.d(String.format("isTimeNextAccessibility:%s", isTimeNextAccessibility))
                return isTimeNextAccessibility
            }
        }
    }

    /**
     * Method to obtain if storage is enabled
     * @param ctx context
     * @return true if storage enabled, false otherwise
     */
    fun isExternalStorageManager(ctx: Context?): Boolean {
        return true
    }

    /**
     * Method that validates if the storage method should request it
     *
     * @param ctx context
     * @return true if storage method should request it, false otherwise
     */
    fun isExternalStorageManagerView(ctx: Context?): Boolean {
        return true
    }

    /**
     * Method that validates if the background location method should request it
     *
     * @param ctx context
     * @return true if background location method should request it, false otherwise
     */
    fun canAccessBackgroundLocationView(ctx: Context): Boolean {
        val canAccessBackgroundLocation = canAccessBackgroundLocation(ctx)
        PreyLogger.d(String.format("canAccessBackgroundLocation:%s", canAccessBackgroundLocation))
        if (canAccessBackgroundLocation) {
            return canAccessBackgroundLocation
        } else {
            val locatinBgDenied = PreyConfig.getInstance(ctx).getLocationBgDenied()
            PreyLogger.d(String.format("locatinBgDenied:%s", locatinBgDenied))
            if (locatinBgDenied) {
                return locatinBgDenied
            } else {
                val isTimeNextLocationBg = PreyConfig.getInstance(ctx).isTimeNextLocationBg()
                PreyLogger.d(String.format("isTimeNextLocationBg:%s", isTimeNextLocationBg))
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
     * @param ctx context
     * @return true if the caller can schedule exact alarms, false otherwise.
     */
    fun canScheduleExactAlarms(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmMgr.canScheduleExactAlarms()
        } else {
            return true
        }
    }
}