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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.js.WebAppInterface;
import com.prey.services.PreyOverlayService;

import java.util.Locale;

public class CheckPasswordHtmlActivity extends AppCompatActivity  implements  FingerprintHelper.FingerprintHelperListener{

    public static final String CLOSE_PREY="close_prey";
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
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.webview);
        PreyLogger.d("CheckPasswordHtmlActivity: onCreate");
        loadUrl();

        registerReceiver(close_prey_receiver, new IntentFilter(CLOSE_PREY));

    }

    @Override
    protected void onResume() {
        super.onResume();

        PreyLogger.d("CheckPasswordHtmlActivity: onResume");
        reload();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(close_prey_receiver);
    }

    public void settings(){
        PreyLogger.d("CheckPasswordHtmlActivity: settings");
        myWebView = (WebView) findViewById(R.id.install_browser);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
    }

    public void loadUrl(){
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this,this), "Android");
        myWebView.loadUrl(getUrl(this));
    }

    public void reload(){
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this,this), "Android");
        myWebView.loadUrl(getUrl(this));
        myWebView.reload();
    }

    public String getUrl(Context ctx){
        String url="";
        if ("es".equals(Locale.getDefault().getLanguage())) {
            url="file:///android_asset/html/protected_es.html";
        } else {
            url="file:///android_asset/html/protected.html";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
            boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
            boolean canAccessCamera = PreyPermission.canAccessCamera(this);
            boolean canAccessReadPhoneState = PreyPermission.canAccessReadPhoneState(this);
            boolean canAccessReadExternalStorage = PreyPermission.canAccessReadExternalStorage(this);
            boolean canDrawOverlays =false;
            if (!canAccessFineLocation || !canAccessCoarseLocation || !canAccessCamera
                    || !canAccessReadPhoneState || !canAccessReadExternalStorage) {
                if ("es".equals(Locale.getDefault().getLanguage())) {
                    url="file:///android_asset/html/un_protected_es.html" ;
                }else{
                    url="file:///android_asset/html/un_protected.html";
                }
            }
        }
        boolean canDrawOverlays=PreyPermission.canDrawOverlays(this);
        if (!canDrawOverlays) {
            if ("es".equals(Locale.getDefault().getLanguage())) {
                url="file:///android_asset/html/un_protected_es.html" ;
            }else{
                url="file:///android_asset/html/un_protected.html";
            }
        }
        PreyLogger.d("url"+url);
        return url;
    }

    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermissionAndroid7() {
        PreyLogger.d("askForPermissionAndroid7");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        startOverlayService();
    }

    private void startOverlayService() {
        PreyLogger.d("startOverlayService");
        Intent intent = new Intent(getApplicationContext(), PreyOverlayService.class);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermission() {
        ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean canDrawOverlays=PreyPermission.canDrawOverlays(this);
        if (!canDrawOverlays) {
            askForPermissionAndroid7();
            startOverlayService();
        }else{
            reload();
        }
    }

    @Override
    public void authenticationFailed(String error) {
        PreyLogger.d("authenticationFailed");
    }

    @Override
    public void authenticationSuccess(FingerprintManager.AuthenticationResult result) {
        PreyLogger.d("authenticationSuccess");
        PreyConfig.getPreyConfig(getApplicationContext()).setTimePasswordOk();
        Intent intent = new Intent(getApplicationContext(), PreyConfigurationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
        getApplicationContext().startActivity(intent);
        finish();
    }

}

