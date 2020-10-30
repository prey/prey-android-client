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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.js.WebAppInterface;
import com.prey.receivers.PreyDisablePowerOptionsReceiver;

public class PreySecureHtmlService extends Service {

    private WindowManager windowManager;
    private View view;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        PreyLogger.d("PreySecureHtmlService onCreate");
    }

    Button button_Super_Lock_Unlock = null;
    TextView textViewPin = null;
    EditText editTextPin = null;

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        PreyLogger.d("PreySecureHtmlService onStart");
        final String pinNumber = PreyConfig.getPreyConfig(ctx).getPinNumber();
        String pinActivated=PreyConfig.getPreyConfig(getApplicationContext()).getPinActivated();
        boolean isPinActivated = pinNumber!=null&&!"".equals(pinNumber)&&pinActivated!=null&&!"".equals(pinActivated);
        if(pinNumber == null || "".equals(pinNumber) ||!isPinActivated){
            PreyConfig.getPreyConfig(ctx).setOverLock(false);
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        }
        String deviceKey = PreyConfig.getPreyConfig(ctx).getDeviceId();
        if (deviceKey != null && !"".equals(deviceKey) && isPinActivated) {
            try {
                Intent intentClose = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                intentClose.putExtra(PreyDisablePowerOptionsReceiver.stringExtra, PreyDisablePowerOptionsReceiver.stringExtra);
                this.sendBroadcast(intentClose);
            } catch (Exception e) {
            }
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.webview, null);
            PreyConfig.getPreyConfig(ctx).viewSecure = view;
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
                    if (wm != null) {
                        try {
                            wm.addView(view, layoutParams);
                            PreyConfig.getPreyConfig(this).setOpenSecureService(true);
                            PreyConfig.getPreyConfig(this).setOverLock(true);
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
                }
            }
            view = null;
        }
    }

}
