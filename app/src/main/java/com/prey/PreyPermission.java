/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;

public class PreyPermission {

    public static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    public static boolean canPermissionGranted(Context ctx,String permission){
        boolean canPermissionGranted=(PermissionChecker.checkSelfPermission(ctx, permission) ==
                PermissionChecker.PERMISSION_GRANTED  );
        return canPermissionGranted;
    }

    public static boolean shouldShowRequestPermission(Activity activity, String permission) {
        boolean shouldShowRequestPermission=false;
        boolean isFirst=PreyConfig.getPreyConfig(activity.getApplicationContext()).getBoolean(permission,true);
        PreyConfig.getPreyConfig(activity.getApplicationContext()).saveBoolean(permission,false);
        if(isFirst){
            shouldShowRequestPermission = true;
        }else {
            shouldShowRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }
        return shouldShowRequestPermission;
    }

    public static boolean canAccessFineLocation(Context ctx) {
        return canPermissionGranted(ctx,android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean showRequestFineLocation(Activity activity) {
        return shouldShowRequestPermission(activity,android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean canAccessCoarseLocation(Context ctx) {
        return canPermissionGranted(ctx,android.Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean showRequestCoarseLocation(Activity activity) {
        return shouldShowRequestPermission(activity,android.Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean canAccessBackgroundLocation(Context ctx) {
        if (Build.VERSION.SDK_INT < PreyConfig.BUILD_VERSION_CODES_10) {
            return true;
        }else {
            return canPermissionGranted(ctx, ACCESS_BACKGROUND_LOCATION);
        }
    }

    public static boolean canAccessCamera(Context ctx) {
        return canPermissionGranted(ctx,android.Manifest.permission.CAMERA);
    }

    public static boolean showRequestCamera(Activity activity) {
        return shouldShowRequestPermission(activity,android.Manifest.permission.CAMERA);
    }

    public static boolean showRequestPhone(Activity activity) {
        return shouldShowRequestPermission(activity,android.Manifest.permission.READ_PHONE_STATE);
    }

    public static boolean canAccessStorage(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return canPermissionGranted(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            boolean image = canPermissionGranted(ctx, android.Manifest.permission.READ_MEDIA_IMAGES);
            boolean audio = canPermissionGranted(ctx, android.Manifest.permission.READ_MEDIA_AUDIO);
            boolean video = canPermissionGranted(ctx, android.Manifest.permission.READ_MEDIA_VIDEO);
            return image && audio && video;
        }
    }

    public static boolean showRequestStorage(Activity activity) {
        return shouldShowRequestPermission(activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean showRequestBackgroundLocation(Activity activity) {
        if (Build.VERSION.SDK_INT < PreyConfig.BUILD_VERSION_CODES_10) {
            return true;
        } else{
            return shouldShowRequestPermission(activity, ACCESS_BACKGROUND_LOCATION);
        }
    }

    public static boolean canDrawOverlays(Context ctx) {
        if(PreyConfig.getPreyConfig(ctx).isChromebook()){
            return true;
        }
        boolean canDrawOverlays=true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canDrawOverlays = Settings.canDrawOverlays(ctx);
        }
        return canDrawOverlays;
    }

    public static  boolean checkBiometricSupport(Context ctx) {
        return BiometricManager.from(ctx).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Method to obtain if accessibility service is enabled
     * @param context
     * @return true if accessibility service enabled, false otherwise
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        String settingValue = Settings.Secure.getString(
                context.getApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue == null)
            return false;
        return settingValue.indexOf("prey") > 0;
    }

    /**
     * Method that validates if the accessibility method should request it
     *
     * @param ctx context
     * @return true if accessibility method should request it, false otherwise
     */
    public static boolean isAccessibilityServiceView(Context ctx) {
        boolean isThereBatchInstallationKey = PreyBatch.isThereBatchInstallationKey(ctx);
        //If it is batch, do not request accessibility
        if (isThereBatchInstallationKey) {
            return isThereBatchInstallationKey;
        }
        boolean isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(ctx);
        PreyLogger.d(String.format("isAccessibilityServiceEnabled:%s", isAccessibilityServiceEnabled));
        if (isAccessibilityServiceEnabled) {
            return isAccessibilityServiceEnabled;
        } else {
            boolean accessibilityDenied = PreyConfig.getPreyConfig(ctx).getAccessibilityDenied();
            PreyLogger.d(String.format("accessibilityDenied:%s", accessibilityDenied));
            if (accessibilityDenied) {
                return accessibilityDenied;
            } else {
                boolean isTimeNextAccessibility = PreyConfig.getPreyConfig(ctx).isTimeNextAccessibility();
                PreyLogger.d(String.format("isTimeNextAccessibility:%s", isTimeNextAccessibility));
                return isTimeNextAccessibility;
            }
        }
    }

    /**
     * Method to obtain if storage is enabled
     * @param ctx context
     * @return true if storage enabled, false otherwise
     */
    public static boolean isExternalStorageManager(Context ctx){
        return true;
    }

    /**
     * Method that validates if the storage method should request it
     *
     * @param ctx context
     * @return true if storage method should request it, false otherwise
     */
    public static boolean isExternalStorageManagerView(Context ctx) {
        return true;
    }

    /**
     * Method that validates if the background location method should request it
     *
     * @param ctx context
     * @return true if background location method should request it, false otherwise
     */
    public static boolean canAccessBackgroundLocationView(Context ctx) {
        boolean canAccessBackgroundLocation = canAccessBackgroundLocation(ctx);
        PreyLogger.d(String.format("canAccessBackgroundLocation:%s", canAccessBackgroundLocation));
        if (canAccessBackgroundLocation) {
            return canAccessBackgroundLocation;
        } else {
            boolean locatinBgDenied = PreyConfig.getPreyConfig(ctx).getLocationBgDenied();
            PreyLogger.d(String.format("locatinBgDenied:%s", locatinBgDenied));
            if (locatinBgDenied) {
                return locatinBgDenied;
            } else {
                boolean isTimeNextLocationBg = PreyConfig.getPreyConfig(ctx).isTimeNextLocationBg();
                PreyLogger.d(String.format("isTimeNextLocationBg:%s", isTimeNextLocationBg));
                return isTimeNextLocationBg;
            }
        }
    }

    public static boolean areNotificationsEnabled(Context context){
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * Method that validates whether exact alarms can be programmed
     *
     * @param ctx context
     * @return true if the caller can schedule exact alarms, false otherwise.
     */
    public static boolean canScheduleExactAlarms(Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            return alarmMgr.canScheduleExactAlarms();
        } else {
            return true;
        }
    }
}
