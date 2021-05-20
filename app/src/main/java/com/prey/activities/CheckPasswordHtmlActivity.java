/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyUtils;
import com.prey.R;

import com.prey.activities.js.CustomWebView;
import com.prey.activities.js.WebAppInterface;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.services.PreyAccessibilityService;
import com.prey.services.PreyOverlayService;

public class CheckPasswordHtmlActivity extends AppCompatActivity {

    public static String JS_ALIAS="Android";
    public static String URL_ONB = "file:///android_asset/html/index.html";

    public static final String CLOSE_PREY = "close_prey";
    private final BroadcastReceiver close_prey_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreyLogger.d("CheckPasswordHtmlActivity BroadcastReceiver: finish");
            finish();
        }
    };

    private WebView myWebView = null;

    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        }catch (Exception e){
            PreyLogger.e("Error ActionBar().hide",e);
        }
        setContentView(R.layout.webview);
        PreyLogger.d("CheckPasswordHtmlActivity: onCreate");
        registerReceiver(close_prey_receiver, new IntentFilter(CLOSE_PREY));
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreyConfig.getPreyConfig(this).setCapsLockOn(false);
        PreyLogger.d("CheckPasswordHtmlActivity: onResume");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        Bundle extras = getIntent().getExtras();
        String nexturl = "";
        try {
            nexturl = extras.getString("nexturl");
        } catch (Exception e) {
            PreyLogger.d("not extra nexturl");
        }
        PreyLogger.d("CheckPasswordHtmlActivity nexturl: " + nexturl);
        if ("tryReport".equals(nexturl)) {
            tryReport();
        } else {
            loadUrl();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(close_prey_receiver);
    }

    public void settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings");
        myWebView = (WebView) findViewById(R.id.install_browser);
        myWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                CustomWebView.callDispatchKeyEvent(getApplicationContext(),keyEvent);
                return false;
            }
        });
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try{
                myWebView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                myWebView.getSettings().setSavePassword(false);
                myWebView.clearFormData();
            }catch (Exception e){
                PreyLogger.e("Error autofill:"+e.getMessage(),e);
            }
        }
    }

    public void tryReport() {
        PreyLogger.d("CheckPasswordHtmlActivity: tryReport");
        String lng = PreyUtils.getLanguage();
        String url = URL_ONB + "#/" + lng + "/activation";
        settings();
        PreyLogger.d("_url:" + url);
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");
    }

    public void loadUrl() {
        PreyLogger.d("CheckPasswordHtmlActivity: loadUrl");
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
        myWebView.loadUrl("javascript:window.location.reload(true)");
    }

    public void reload() {
        PreyLogger.d("CheckPasswordHtmlActivity: reload");
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
        myWebView.reload();
    }

    public String getUrl(Context ctx) {
        String lng = PreyUtils.getLanguage();
        String url = "";
        String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();
        PreyLogger.d("CheckPasswordHtmlActivity deviceKey:" + deviceKey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES >=M" );
            boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
            boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
            boolean canAccessCamera = PreyPermission.canAccessCamera(this);
            boolean canAccessStorage = PreyPermission.canAccessStorage(this);
            boolean canAccessBackgroundLocation =PreyPermission.canAccessBackgroundLocation(this);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessFineLocation:" + canAccessFineLocation);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessCoarseLocation:" + canAccessCoarseLocation);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessCamera:" + canAccessCamera);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessStorage:" + canAccessStorage);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessBackgroundLocation:" + canAccessBackgroundLocation);
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
            PreyLogger.d("CheckPasswordHtmlActivity: canDrawOverlays:" + canDrawOverlays);
            boolean canAccessibility = PreyPermission.isAccessibilityServiceEnabled(this);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessibility:" + canAccessibility);
            boolean isAdminActive = FroyoSupport.getInstance(this).isAdminActive();
            PreyLogger.d("CheckPasswordHtmlActivity: isAdminActive:" + isAdminActive);
            boolean configurated=(canAccessFineLocation||canAccessCoarseLocation) && canAccessBackgroundLocation && canAccessCamera
                    && canAccessStorage && isAdminActive && canDrawOverlays && canAccessibility;
            String installationStatus=PreyConfig.getPreyConfig(this).getInstallationStatus();
            PreyLogger.d(String.format("CheckPasswordHtmlActivity: configurated:%s installationStatus:%s",configurated,installationStatus));
            if (configurated) {
                    if (deviceKey != null && !"".equals(deviceKey)) {
                        if ("".equals(installationStatus)) {
                            url = URL_ONB + "#/" + lng + "/";
                        }else{
                            if ("OK".equals(installationStatus)) {
                                PreyConfig.getPreyConfig(ctx).setInstallationStatus("");
                                url = URL_ONB + "#/" + lng + "/emailok";
                            }else {
                                url = URL_ONB + "#/" + lng + "/emailsent";
                            }
                        }
                    } else {
                        if ("DEL".equals(installationStatus)) {
                            PreyConfig.getPreyConfig(ctx).setInstallationStatus("");
                            url = URL_ONB + "#/" + lng + "/emailretry";
                        }else {
                            url = URL_ONB + "#/" + lng + "/signin";
                        }
                    }
            } else {
                boolean permissions=(canAccessFineLocation||canAccessCoarseLocation) && canAccessCamera
                        && canAccessStorage && isAdminActive && canDrawOverlays && canAccessibility;
                boolean permissions2=(canAccessFineLocation||canAccessCoarseLocation) ||  canAccessCamera
                        || canAccessStorage  || isAdminActive || canDrawOverlays || canAccessibility;
                PreyLogger.d("CheckPasswordHtmlActivity permissions:"+permissions);
                PreyLogger.d("CheckPasswordHtmlActivity canAccessBackgroundLocation:"+canAccessBackgroundLocation);
                if (permissions) {
                    if(canAccessBackgroundLocation) {
                        url = URL_ONB + "#/" + lng + "/permissions";
                    }else {
                        url = URL_ONB + "#/" + lng + "/bgloc";
                    }
                } else {
                    if(permissions2){
                        url = URL_ONB + "#/" + lng + "/permissions";
                    }else{
                        url = URL_ONB + "#/" + lng + "/start";
                    }
                }
            }
        }else{
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES <M" );
            if (deviceKey != null && deviceKey != ""  ) {
                url = URL_ONB + "#/" + lng + "/";
            } else {
                url = URL_ONB + "#/" + lng + "/signin";
            }
        }
        PreyLogger.d("_url:" + url);
        return url;
    }

    private static final int REQUEST_PERMISSIONS = 5;
    private static final int REQUEST_PERMISSIONS_LOCATION = 6;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermissionAndroid7() {
        PreyLogger.d("CheckPasswordHtmlActivity: askForPermissionAndroid7");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        startOverlayService();
    }

    private void startOverlayService() {
        PreyLogger.d("CheckPasswordHtmlActivity: startOverlayService");
        Intent intentOverlay = new Intent(getApplicationContext(), PreyOverlayService.class);
        startService(intentOverlay);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermission() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermission");
        ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }

    public void deniedPermission() {
        PreyLogger.d("deniedPermission");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String permission="";
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
        if(!canAccessFineLocation||!canAccessCoarseLocation) {
            permission = this.getString(R.string.permission_location);
        }else{
            boolean canAccessBackgroundLocation = PreyPermission.canAccessBackgroundLocation(this);
            if(!canAccessBackgroundLocation) {
                permission = this.getString(R.string.permission_location);
            }
        }
        boolean canAccessCamera = PreyPermission.canAccessCamera(this);
        if(!canAccessCamera) {
            if(!"".equals(permission))
                permission += ", ";
            permission += this.getString(R.string.permission_camera);
        }
        boolean canAccessReadPhoneState = PreyPermission.canAccessPhone(this);
        if(!canAccessReadPhoneState) {
            if(!"".equals(permission))
                permission += ", ";
            permission += this.getString(R.string.permission_phone);
        }
        boolean canAccessWriteExternalStorage = PreyPermission.canAccessStorage(this);
        if(!canAccessWriteExternalStorage) {
            if(!"".equals(permission))
                permission += ", ";
            permission += this.getString(R.string.permission_storage);
        }
        PreyLogger.d("permission:"+permission);
        String message ="";
        try {
            message = String.format(getResources().getString(R.string.permission_message_popup),permission);
        }catch (Exception e){
            PreyLogger.e("Error format:"+e.getMessage(),e);
        }
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.permission_manually, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intentSetting = new Intent();
                intentSetting.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intentSetting.setData(uri);
                startActivity(intentSetting);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        PreyLogger.d("CheckPasswordHtmlActivity onRequestPermissionsResult:"+requestCode);
        if(requestCode==REQUEST_PERMISSIONS) {
            for (int i = 0; permissions != null && i < permissions.length; i++) {
                PreyLogger.d("CheckPasswordHtmlActivity onRequestPermissionsResult:" + permissions[i] + " " + grantResults[i]);
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(false);
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[i] == 0) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(true);
                }
            }
        }
        if(requestCode==REQUEST_PERMISSIONS_LOCATION) {
            for (int i = 0; permissions != null && i < permissions.length; i++) {
                PreyLogger.d("CheckPasswordHtmlActivity onRequestPermissionsResult:[" + i + "]" + grantResults[i]);
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(false);
                }
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(true);
                }
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == -1) {
                    PreyConfig.getPreyConfig(this).setPermissionLocation(true);
                }
            }
        }
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
        boolean canAccessCamera = PreyPermission.canAccessCamera(this);
        boolean canAccessPhone = PreyPermission.canAccessPhone(this);
        boolean canAccessStorage = PreyPermission.canAccessStorage(this);
        if (canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
                && canAccessPhone && canAccessStorage  ) {
            PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 1");
            boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
            if (!canDrawOverlays) {
                PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 2");
                askForPermissionAndroid7();
                startOverlayService();
            } else {
                PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 3");
                if (!canDrawOverlays) {
                    PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 4");
                    askForAdminActive();
                } else {
                    PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 5");
                    Intent intentLogin = new Intent(this, LoginActivity.class);
                    intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentLogin);
                    finish();
                }
            }
        }
    }

    private static final int SECURITY_PRIVILEGES = 10;
    public void askForAdminActive() {
        Intent intentAskForAdmin = FroyoSupport.getInstance(getApplicationContext()).getAskForAdminPrivilegesIntent();
        startActivityForResult(intentAskForAdmin, SECURITY_PRIVILEGES);
    }

    public void accessibility() {
        PreyLogger.d("CheckPasswordHtmlActivity accessibility");
        Intent intentService = new Intent(getApplicationContext(), PreyAccessibilityService.class);
        startService(intentService);
        Intent intentSetting = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentSetting);
    }

    public void askForPermissionLocation() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermissionLocation");
        ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSIONS_LOCATION);
    }

}