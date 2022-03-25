/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
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
        return canPermissionGranted(ctx,android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        return false;
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

    public static boolean isAccessibilityServiceView(Context ctx) {
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
}
