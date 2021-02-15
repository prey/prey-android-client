/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2021 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.activities.CloseActivity;
import com.prey.activities.PanelWebActivity;
import com.prey.activities.SecurityActivity;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
import com.prey.services.PreyLockHtmlService;

public class CustomWebView extends WebView {
    public CustomWebView(Context context) {
        super(context);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        PreyConfig.getPreyConfig(getContext()).setInputWebview("");
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        BaseInputConnection baseInputConnection = new BaseInputConnection(this, false);
        return baseInputConnection;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean dispatchFirst = super.dispatchKeyEvent(event);
        callDispatchKeyEvent(getContext(),event);
        return dispatchFirst;
    }

    public static void callDispatchKeyEvent(final Context ctx,KeyEvent event){
        PreyLogger.d("callDispatchKeyEvent:"+event.getKeyCode());
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            String page = PreyConfig.getPreyConfig(ctx).getPage();
            String apikey = PreyConfig.getPreyConfig(ctx).getApiKey();
            String inputWebview = PreyConfig.getPreyConfig(ctx).getInputWebview();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            PreyLogger.d("CustomWebView dispatchKeyEvent Enter page:" + page + " inputWebview:" + inputWebview);
            if ("setting".equals(page)) {
                try {
                    boolean isPasswordOk = PreyWebServices.getInstance().checkPassword(ctx, apikey, inputWebview);
                    if (isPasswordOk) {
                        PreyConfig.getPreyConfig(ctx).setUnlockPass("");
                        Intent intent2 = new Intent(ctx, SecurityActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent2);
                    }
                } catch (Exception e) {
                }
            }
            if ("login".equals(page)) {
                try {
                    boolean isPasswordOk = PreyWebServices.getInstance().checkPassword(ctx, apikey, inputWebview);
                    if (isPasswordOk) {
                        PreyConfig.getPreyConfig(ctx).setUnlockPass("");
                        Intent intent2 = new Intent(ctx, PanelWebActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent2);
                    }
                } catch (Exception e) {
                }
            }
            if ("lock".equals(page)) {
                String unlock = PreyConfig.getPreyConfig(ctx).getUnlockPass();
                PreyLogger.d("dispatchKeyEvent inputWebview:" + inputWebview + " unlock:" + unlock);
                PreyConfig.getPreyConfig(ctx).setInputWebview("");
                if (unlock != null && !"".equals(unlock) && unlock.equals(inputWebview)) {
                    PreyConfig.getPreyConfig(ctx).setUnlockPass("");
                    Intent intent = new Intent(ctx, PreyLockHtmlService.class);
                    ctx.stopService(intent);
                    new Thread() {
                        public void run() {
                            String jobIdLock = PreyConfig.getPreyConfig(ctx).getJobIdLock();
                            String reason = "{\"origin\":\"user\"}";
                            if (jobIdLock != null && !"".equals(jobIdLock)) {
                                reason = "{\"origin\":\"user\",\"device_job_id\":\"" + jobIdLock + "\"}";
                                PreyConfig.getPreyConfig(ctx).setJobIdLock("");
                            }
                            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped", reason));
                        }
                    }.start();
                    Intent intent2 = new Intent(ctx, CloseActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent2);
                    try {
                        View viewLock = PreyConfig.getPreyConfig(ctx).viewLock;
                        if (viewLock != null) {
                            WindowManager wm = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
                            wm.removeView(viewLock);
                        } else {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    } catch (Exception e) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
            }
        }
    }
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
}