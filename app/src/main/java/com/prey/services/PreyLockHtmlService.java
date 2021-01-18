/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.js.WebAppInterface;

public class PreyLockHtmlService extends Service{

    private View view;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public PreyLockHtmlService getService() {
            return PreyLockHtmlService.this;
        }
    }

    public void stop() {
        stopSelf();
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onCreate() {
        super.onCreate();
        PreyLogger.d("PreyLockHtmlService onCreate");
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent,startId);
        final Context ctx=this;
        PreyLogger.d("PreyLockHtmlService onStart");
        final String unlock= PreyConfig.getPreyConfig(ctx).getUnlockPass();
        if(unlock!=null&&!"".equals(unlock)) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.webview, null);
            PreyConfig.getPreyConfig(ctx).viewLock=view;

            WebView myWebView = (WebView) view.findViewById(R.id.install_browser);
            WebSettings settings = myWebView.getSettings();
            myWebView.setBackgroundColor(0x00000000);
            settings.setJavaScriptEnabled(true);
            settings.setLoadWithOverviewMode(true);
            settings.setLoadsImagesAutomatically(true);
            settings.setUseWideViewPort(true);
            settings.setSupportZoom(false);
            settings.setBuiltInZoomControls(false);

            String lng = PreyUtils.getLanguage();
            String url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/lock";

            myWebView.addJavascriptInterface(new WebAppInterface(this), CheckPasswordHtmlActivity.JS_ALIAS);
            myWebView.loadUrl(url);
            myWebView.loadUrl("javascript:window.location.reload(true)");

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                if (Settings.canDrawOverlays(this)) {
                    if(wm != null) {
                        try{
                            wm.addView(view, layoutParams);
                            PreyConfig.getPreyConfig(this).setOpenSecureService(true);
                            PreyConfig.getPreyConfig(this).setOverLock(true);
                        }catch (Exception e){
                            PreyLogger.e(e.getMessage(),e);
                        }
                    }
                }
            }
        }else{
            stopSelf();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        PreyLogger.d("PreyLockHtmlService onDestroy");
    }

}