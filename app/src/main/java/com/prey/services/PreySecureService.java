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
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.js.CustomWebView;
import com.prey.activities.js.WebAppInterface;
import com.prey.receivers.PreyDisablePowerOptionsReceiver;

import java.util.Date;

public class PreySecureService extends Service {

    private WindowManager windowManager;
    private View view;

    public PreySecureService() {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        PreyLogger.d("PreySecureService onCreate");
    }

    private WebView myWebView = null;

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        PreyLogger.d("PreySecureService onStart");
        boolean canDrawOverlays = PreyPermission.canDrawOverlays(ctx);
        if (!canDrawOverlays) {
            stopSelf();
            return;
        }
        long time = PreyConfig.getPreyConfig(this).getTimeSecureLock();
        long now = new Date().getTime();
        PreyLogger.d(String.format("PreyDisablePowerOptionsReceiver time:%s now:%s <%s", time, now, (now < time)));
        if (now < time) {
            PreyLogger.d("PreySecureService close");
            stopSelf();
            return;
        }
        boolean viewSecure = PreyConfig.getPreyConfig(this).getViewSecure();
        if (!viewSecure) {
            PreyLogger.d("PreySecureService viewSecure stopSelf");
            stopSelf();
            return;
        }
        PreyConfig.getPreyConfig(this).setViewSecure(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.webview, null);
        myWebView = (WebView) view.findViewById(R.id.install_browser);
        PreyConfig.getPreyConfig(this).viewSecure = myWebView;
        myWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                CustomWebView.callDispatchKeyEvent(getApplicationContext(), keyEvent);
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
        String lng = PreyUtils.getLanguage();
        String url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/pin";
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), CheckPasswordHtmlActivity.JS_ALIAS);
        myWebView.loadUrl(url);
        myWebView.loadUrl("javascript:window.location.reload(true)");
        final String pinNumber = PreyConfig.getPreyConfig(ctx).getPinNumber();
        if (pinNumber != null && !"".equals(pinNumber) && pinNumber.length() == 4) {
            try {
                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                intentClose.putExtra(PreyDisablePowerOptionsReceiver.stringExtra, PreyDisablePowerOptionsReceiver.stringExtra);
                this.sendBroadcast(intentClose);
            } catch (Exception e) {
                PreyLogger.e(String.format("Error CLOSE_SYSTEM:%s", e.getMessage()), e);
            }
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
                    if (wm != null) {
                        try {
                            wm.addView(view, layoutParams);
                            PreyConfig.getPreyConfig(this).setOpenSecureService(true);
                        } catch (Exception e) {
                            PreyLogger.e(e.getMessage(), e);
                        }
                    }
                }
            }
            try {
                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                intentClose.putExtra(PreyDisablePowerOptionsReceiver.stringExtra, PreyDisablePowerOptionsReceiver.stringExtra);
                this.sendBroadcast(intentClose);
            } catch (Exception e) {
                PreyLogger.e(String.format("Error intentClose:%s", e.getMessage()), e);
            }
        } else {
            if (view != null) {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                if (wm != null) {
                    wm.removeView(view);
                }
                view = null;
            }
            PreyConfig.getPreyConfig(this).setOpenSecureService(false);
            stopSelf();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        PreyLogger.d("PreySecureService onDestroy");
        PreyConfig.getPreyConfig(this).setOpenSecureService(false);
        if (view != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (wm != null) {
                try {
                    wm.removeView(view);
                } catch (Exception e) {
                    PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
                }
            }
            view = null;
        }
    }

    public void stop() {
        close();
    }

    public void close() {
        PreyLogger.d("PreySecureService close");
        stopSelf();
    }

}