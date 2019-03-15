/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.PanelWebActivity;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.activities.FingerprintAuthenticationDialogFragment;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.activities.FingerprintHelper;
import com.prey.net.PreyWebServices;

public class WebAppInterface {

    Context mContext;
    int wrongPasswordIntents = 0;

    private CheckPasswordHtmlActivity mActivity;

    public WebAppInterface(Context context, CheckPasswordHtmlActivity activity) {
        mContext = context;
        mActivity=activity;
    }

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";

    @JavascriptInterface
    public void open_panel(){
        FingerprintAuthenticationDialogFragment fragment
                = new FingerprintAuthenticationDialogFragment();
        Bundle bundle=new Bundle();
        bundle.putString("from","panel");
        fragment.setArguments(bundle);
        fragment.show(mActivity.getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }
    @JavascriptInterface
    public void open_setting(){
        FingerprintAuthenticationDialogFragment fragment
                = new FingerprintAuthenticationDialogFragment();
        Bundle bundle=new Bundle();
        bundle.putString("from","setting");
        fragment.setArguments(bundle);
        fragment.show(mActivity.getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    @JavascriptInterface
    public void givePermissions() {
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(mContext);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(mContext);
        boolean canAccessCamera = PreyPermission.canAccessCamera(mContext);
        boolean canAccessReadPhoneState = PreyPermission.canAccessReadPhoneState(mContext);
        boolean canAccessReadExternalStorage = PreyPermission.canAccessReadExternalStorage(mContext);
        PreyLogger.d("canAccessFineLocation:"+canAccessFineLocation);
        PreyLogger.d("canAccessCoarseLocation:"+canAccessCoarseLocation);
        PreyLogger.d("canAccessCamera:"+canAccessCamera);
        PreyLogger.d("canAccessReadPhoneState:"+canAccessReadPhoneState);
        PreyLogger.d("canAccessReadExternalStorage2:"+canAccessReadExternalStorage);
        boolean canDrawOverlays =false;
        //  boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
        //PreyLogger.d("canDrawOverlays:"+canDrawOverlays);
        if (!canAccessFineLocation || !canAccessCoarseLocation || !canAccessCamera
                || !canAccessReadPhoneState || !canAccessReadExternalStorage) {
            PreyLogger.d("dentro");
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            final FrameLayout frameView = new FrameLayout(mContext);
            builder.setView(frameView);

            final AlertDialog alertDialog = builder.create();
            LayoutInflater inflater = alertDialog.getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.warning, frameView);

            Button button_ok = (Button) dialoglayout.findViewById(R.id.button_ok);
            Button button_close = (Button) dialoglayout.findViewById(R.id.button_close);

            button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreyLogger.d("askForPermission");
                    mActivity.askForPermission();
                    alertDialog.dismiss();
                }
            });

            button_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreyLogger.d("close ask");
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }else{
            mActivity.askForPermissionAndroid7();
        }
    }

    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

}
