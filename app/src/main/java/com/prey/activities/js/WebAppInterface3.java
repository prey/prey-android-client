/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.actions.aware.AwareController;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.InitActivity;
import com.prey.activities.PanelWebActivity;
import com.prey.activities.PermissionInformationActivity;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.activities.SecurityActivity;
import com.prey.activities.WelcomeActivity;
import com.prey.barcodereader.BarcodeActivity;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.json.actions.Detach;
import com.prey.net.PreyWebServices;
import com.prey.preferences.DetachDevicePreferences;
import com.prey.preferences.RunBackgroundCheckBoxPreference;

import java.util.HashMap;
import java.util.Map;

public class WebAppInterface3 {

    Context mContext;
    int wrongPasswordIntents = 0;

    private SecurityActivity mActivity;

    public WebAppInterface3(Context context, SecurityActivity activity) {
        mContext = context;
        mActivity=activity;
    }



    @JavascriptInterface
    public void oso(){
        PreyLogger.i("oso");
    }


    @JavascriptInterface
    public void runInBg(boolean runInBg){
        PreyLogger.i("runInBg:"+runInBg);
    }

    @JavascriptInterface
    public void blockUninstall(boolean runInBg){
        PreyLogger.i("blockUninstall:"+runInBg);
    }

    @JavascriptInterface
    public void shieldOffBbtn(boolean runInBg){
        PreyLogger.i("shieldOffBbtn:"+runInBg);
    }

    @JavascriptInterface
    public void wipe(){
        PreyLogger.i("wipe:");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new DetachDevice().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new DetachDevice().execute();
    }

    @JavascriptInterface
    public void savepin(String pin){
        PreyLogger.i("savepin:"+pin);
    }




    public class DetachDevice extends AsyncTask<Void, Void, Void> {

        private String error = null;
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(mContext.getText(R.string.preferences_detach_dettaching_message).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... unused) {
            error= Detach.detachDevice(mContext);
            PreyLogger.d("error:"+error);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
            if (error != null) {
                Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
            } else {
                Intent welcome = new Intent(mContext, CheckPasswordHtmlActivity.class);
                mContext.startActivity(welcome);
                mActivity.finish();
            }
        }

    }
}
