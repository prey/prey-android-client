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
import com.prey.activities.PreReportActivity;
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
    public void report(){
        PreyLogger.i("report:");
        Intent intent = new Intent(mContext, PreReportActivity.class);
        mContext.startActivity(intent);
        mActivity.finish();
    }



    @JavascriptInterface
    public boolean initBackground(){
        boolean initBackground=true;
        PreyLogger.i("initBackground:"+initBackground);
        return initBackground;
    }

    @JavascriptInterface
    public boolean initPin(){
        boolean initPin=true;
        PreyLogger.i("initPin:"+initPin);
        return initPin;
    }

    @JavascriptInterface
    public String getPin(){
        String pin="1234";
        PreyLogger.i("getPin:"+pin);
        return pin;
    }


    @JavascriptInterface
    public boolean initUninstall(){
        boolean initUnis=true;
        PreyLogger.i("initUninstall:"+initUnis);
        return initUnis;
    }

    @JavascriptInterface
    public boolean initShield(){
        boolean initShi=true;
        PreyLogger.i("initShield:"+initShi);
        return initShi;
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


    @JavascriptInterface
    public void login_tipo(String password,String tipo){
        PreyLogger.i("login_tipo:"+ password+" tipo:"+tipo);
        from=tipo;
        String passwordtyped2="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CheckPassword(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,password,passwordtyped2);
        else
            new CheckPassword(mContext).execute(password,passwordtyped2);

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

    String from="setting";

    public class CheckPassword extends AsyncTask<String, Void, Void> {
        private Context mCtx;
        CheckPassword(Context ctx){
            mCtx =ctx;
        }

        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        String error = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                isPasswordOk = false;
                String apikey = PreyConfig.getPreyConfig(mCtx).getApiKey();
                boolean twoStep=PreyConfig.getPreyConfig(mCtx).getTwoStep();
                PreyLogger.d("twoStep:" +twoStep);
                if (twoStep) {
                    PreyLogger.d("apikey:" + apikey + " password:" + password[0] + " password2:" + password[1] );
                    isPasswordOk =PreyWebServices.getInstance().checkPassword2(mCtx,apikey, password[0],password[1]);
                }else{
                    PreyLogger.d("apikey:" + apikey + " password:" + password[0] );
                    isPasswordOk =PreyWebServices.getInstance().checkPassword(mCtx,apikey, password[0]);
                }
                if(isPasswordOk) {
                    PreyConfig.getPreyConfig(mCtx).setTimePasswordOk();
                }
            } catch (Exception e) {
                PreyLogger.e("error:"+e.getMessage(),e);
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
            }
            if (error != null)
                Toast.makeText(mCtx, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {
                wrongPasswordIntents++;
                if (wrongPasswordIntents == 3) {
                    Toast.makeText(mCtx, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mCtx, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                }
            } else {
                PreyLogger.d("from:"+from);
                if("setting".equals(from)) {
                    Intent intent = new Intent(mCtx, SecurityActivity.class);
                    PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mCtx.startActivity(intent);
                    new Thread(new EventManagerRunner(mCtx, new Event(Event.APPLICATION_OPENED))).start();
                    mActivity.finish();
                }else {
                    Intent intent = new Intent(mCtx, PanelWebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mCtx.startActivity(intent);
                    mActivity.finish();
                }
            }
        }
    }

}
