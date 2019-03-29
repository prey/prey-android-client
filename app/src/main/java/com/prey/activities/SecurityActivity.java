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
import com.prey.activities.js.WebAppInterface2;
import com.prey.activities.js.WebAppInterface3;
import com.prey.services.PreyOverlayService;

import java.util.Date;
import java.util.Locale;

public class SecurityActivity extends AppCompatActivity  {


    private WebView myWebView = null;

    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;



    public void onBackPressed() {
        Intent intent = null;
        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);
        startActivity(intent);
        finish();

    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.webview);
        PreyLogger.d("SecurityActivity: onCreate");
        loadUrl();



    }
    public void loadUrl(){
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface3(this,this), "Android");
        myWebView.loadUrl(getUrl(this));
    }



    public void settings(){
        PreyLogger.d("SecurityActivity: settings");
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

    public String getUrl(Context ctx){
        String url="";

                url = "http://10.10.2.32:1337/#/onboarding/android/security?time=" + new Date().getTime();


        PreyLogger.d("url"+url);
        return url;
    }


}