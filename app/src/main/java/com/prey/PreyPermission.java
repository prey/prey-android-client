/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

public class PreyPermission {

    public static boolean canAccessFineLocation(Context ctx) {
        boolean canAccessFineLocation=(PermissionChecker
                .checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED  );
        //PreyLogger.d("canAccessFineLocation:" + canAccessFineLocation);
        return canAccessFineLocation;
    }

    public static boolean canAccessCoarseLocation(Context ctx) {
        boolean canAccessCoarseLocation= PermissionChecker
                .checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessCoarseLocation:"+canAccessCoarseLocation);
        return canAccessCoarseLocation;
    }

    public static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    public static boolean canAccessBackgroundLocation(Context ctx) {
        boolean canAccessBackgroundLocation =true;
        if (PreyConfig.getPreyConfig(ctx).isAndroid10OrAbove()) {
            canAccessBackgroundLocation = PermissionChecker
                    .checkSelfPermission(ctx, ACCESS_BACKGROUND_LOCATION) ==
                    PermissionChecker.PERMISSION_GRANTED;
        }
        //PreyLogger.d("canAccessBackgroundLocation:"+canAccessBackgroundLocation);
        return canAccessBackgroundLocation;
    }


    public static boolean canAccessCamera(Context ctx) {
        boolean canAccessCamera= PermissionChecker
                .checkSelfPermission(ctx, android.Manifest.permission.CAMERA) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessCamera:"+canAccessCamera);
        return canAccessCamera;
    }


    public static boolean canAccessReadPhoneState(Context ctx) {
        boolean canAccessReadPhoneState= PermissionChecker
                .checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessReadPhoneState:"+canAccessReadPhoneState);
        return canAccessReadPhoneState;
    }

    public static boolean canAccessSendSms(Context ctx) {
        boolean canAccessSendSms= PermissionChecker
                .checkSelfPermission(ctx, Manifest.permission.SEND_SMS) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessSendSms:"+canAccessSendSms);
        return canAccessSendSms;
    }

    public static boolean canAccessReceiveSms(Context ctx) {
        boolean canAccessReceiveSms= PermissionChecker
                .checkSelfPermission(ctx, Manifest.permission.RECEIVE_SMS) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessReceiveSms:"+canAccessReceiveSms);
        return canAccessReceiveSms;
    }

    public static boolean canAccessReadSms(Context ctx) {
        boolean canAccessReadSms= PermissionChecker
                .checkSelfPermission(ctx, Manifest.permission.READ_SMS) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessReadSms:"+canAccessReadSms);
        return canAccessReadSms;
    }

    public static boolean canAccessReadExternalStorage(Context ctx) {
        boolean canAccessReadExternalStorage= PermissionChecker
                .checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessReadExternalStorage:"+canAccessReadExternalStorage);
        return canAccessReadExternalStorage;
    }

    public static boolean canAccessWriteExternalStorage(Context ctx) {
        boolean canAccessWriteExternalStorage= PermissionChecker
                .checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PermissionChecker.PERMISSION_GRANTED;
        //PreyLogger.d("canAccessWriteExternalStorage:"+canAccessWriteExternalStorage);
        return canAccessWriteExternalStorage;
    }


    public static boolean canDrawOverlays(Context ctx) {
        boolean canDrawOverlays=true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canDrawOverlays = Settings.canDrawOverlays(ctx);
        }
        return canDrawOverlays;
    }

    public static  boolean checkBiometricSupport(Context ctx) {
        return false;/*
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(ctx.KEYGUARD_SERVICE);
        PackageManager packageManager = ctx.getPackageManager();
        if (!keyguardManager.isKeyguardSecure()) {
            PreyLogger.d("Lock screen security not enabled in Settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(ctx,Manifest.permission.USE_FINGERPRINT) !=PackageManager.PERMISSION_GRANTED) {
            PreyLogger.d("Fingerprint authentication permission not enabled");
            return false;
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            PreyLogger.d("Fingerprint hasSystemFeature");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) ctx.getSystemService(ctx.FINGERPRINT_SERVICE);
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                PreyLogger.d( "User hasn't registered any fingerprints");
                return false;
            }
        }

        return true;
        */
    }

}
