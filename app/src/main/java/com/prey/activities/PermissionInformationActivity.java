/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.prey.PreyConfig;
import com.prey.PreyPermission;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;

import androidx.core.app.ActivityCompat;

import com.prey.PreyLogger;
import com.prey.actions.aware.AwareController;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.services.PreyOverlayService;

public class PermissionInformationActivity extends PreyActivity {

    private static final int SECURITY_PRIVILEGES = 10;
    private String congratsMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        showScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PreyLogger.d("requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == SECURITY_PRIVILEGES)
            showScreen();
    }

    private void showScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyLogger.d("PermissionInformationActivity: Build.VERSION_CODES >=M");
            boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
            boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
            boolean canAccessCamera = PreyPermission.canAccessCamera(this);
            boolean canAccessStorage = PreyPermission.canAccessStorage(this);
            boolean configurated=canAccessFineLocation&&canAccessCoarseLocation && canAccessCamera
                    && canAccessStorage ;
            if(!configurated){
                askForPermission();
            }
        }
        if (FroyoSupport.getInstance(this).isAdminActive()) {
                Intent intentPermission = null;
                boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
                if(!canDrawOverlays) {
                    askForPermissionAndroid7();
                    startOverlayService();
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intentPermission = new Intent(PermissionInformationActivity.this, CheckPasswordHtmlActivity.class);
                    } else {
                        intentPermission = new Intent(PermissionInformationActivity.this, LoginActivity.class);
                    }
                }
                PreyConfig.getPreyConfig(PermissionInformationActivity.this).setProtectReady(true);
                new Thread() {
                    public void run() {
                        try{
                            AwareController.getInstance().init(getApplicationContext());
                        }catch(Exception e){
                            PreyLogger.e("Error:"+e.getMessage(),e);
                        }
                    }
                }.start();
                try {
                    if(intentPermission!=null) {
                        startActivity(intentPermission);
                    }
                }catch (Exception e){
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
                finish();
            } else {
                Intent intentPrivileges = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
                startActivityForResult(intentPrivileges, SECURITY_PRIVILEGES);
            }
    }
    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermission() {
        ActivityCompat.requestPermissions(PermissionInformationActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }
    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermissionAndroid7() {
        PreyLogger.d("PermissionInformationActivity: askForPermissionAndroid7");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        startOverlayService();
    }
    public static int OVERLAY_PERMISSION_REQ_CODE = 5473;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        PreyLogger.d("PermissionInformationActivity: onRequestPermissionsResult");
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
        boolean canAccessCamera = PreyPermission.canAccessCamera(this);
        boolean canAccessStorage = PreyPermission.canAccessStorage(this);
        if (canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
                && canAccessStorage  ) {
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
            if (!canDrawOverlays) {
                askForPermissionAndroid7();
                startOverlayService();
            } else {
                if (!canDrawOverlays) {
                    askForAdminActive();
                } else {
                        finish();
                        Intent intent = null;
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent = new Intent(this, CheckPasswordHtmlActivity.class);
                        }else{
                            intent = new Intent(this, CheckPasswordActivity.class);
                        }
                        startActivity(intent);
                }
            }
        }
    }

    private void startOverlayService() {
        PreyLogger.d("PermissionInformationActivity: startOverlayService");
        Intent intentOverlay = new Intent(getApplicationContext(), PreyOverlayService.class);
        startService(intentOverlay);
    }

    public void askForAdminActive() {
        Intent intentPrivileges = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
        startActivityForResult(intentPrivileges, SECURITY_PRIVILEGES);
    }

}