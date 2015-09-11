/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.javascript.WebAppInterface;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class WebViewInitActivity extends Activity {

    private String error = null;
    @Override
    public void onResume() {
        PreyLogger.i("onResume of WebViewInitActivity");
        super.onResume();
        init();
    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of WebViewInitActivity");
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setContentView(R.layout.activity_webview);
        PreyLogger.i("onCreate of WebViewInitActivity");

    }

    private void init(){
        WebView myWebView = (WebView) findViewById(R.id.install_browser);
        WebAppInterface webInterface=new WebAppInterface(this);
        webInterface.setActivityInit(this);
        myWebView.addJavascriptInterface(webInterface, "Android");
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("file:///android_asset/www/init.html");
    }

    public void onBackPressed() {
        init();
    }



    public void addDeviceToAccount(String email,String password){
        new AddDeviceToAccount().execute(email, password);
    }

    public void createAccount(String name,String email,String password){
        new CreateAccount().execute(name,email, password);
    }

    private class AddDeviceToAccount extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog = null;
        private boolean noMoreDeviceError = false;
        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(WebViewInitActivity.this);
                progressDialog.setMessage(WebViewInitActivity.this.getText(R.string.set_old_user_loading).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }catch (Exception e){

            }
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                noMoreDeviceError = false;
                error = null;

                String email=data[0];
                String password=data[1];

                PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(WebViewInitActivity.this, email, password, PreyUtils.getDeviceType(WebViewInitActivity.this));
                PreyConfig.getPreyConfig(WebViewInitActivity.this).saveAccount(accountData);

            } catch (PreyException e) {
                error = e.getMessage();
                PreyLogger.i("error:"+error);
                try {
                    NoMoreDevicesAllowedException noMoreDevices = (NoMoreDevicesAllowedException) e;
                    noMoreDeviceError = true;

                } catch (ClassCastException e1) {
                    noMoreDeviceError = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if(progressDialog!=null)
                    progressDialog.dismiss();
            } catch (Exception e) {
            }
            if (noMoreDeviceError){
                new AlertDialog.Builder(WebViewInitActivity.this).setIcon(R.drawable.info).setTitle(R.string.set_old_user_no_more_devices_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create();
            }


            else {
                if (error == null) {
                    String message = WebViewInitActivity.this.getString(R.string.device_added_congratulations_text);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", message);
                    Intent intent = new Intent(WebViewInitActivity.this, PermissionInformationActivity.class);
                    intent.putExtras(bundle);
                    WebViewInitActivity.this.startActivity(intent);
                    finish();
                } else{
                    PreyLogger.i("___:"+error);
                    new AlertDialog.Builder(WebViewInitActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false).create().show();
                }
            }
        }
    }
    private class CreateAccount extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog = null;
        String email=null;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(WebViewInitActivity.this);
            progressDialog.setMessage(WebViewInitActivity.this.getText(R.string.creating_account_please_wait).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(String... data) {
            try {
                error = null;
                String name=data[0];
                String email=data[1];
                String password=data[2];

                PreyAccountData accountData = PreyWebServices.getInstance().registerNewAccount(WebViewInitActivity.this, name, email, password, PreyUtils.getDeviceType(WebViewInitActivity.this));
                PreyLogger.d("Response creating account: " + accountData.toString());
                PreyConfig.getPreyConfig(WebViewInitActivity.this).saveAccount(accountData);
            } catch (PreyException e) {
                error = e.getMessage();
                PreyLogger.i("error:"+error);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
            if (error == null) {
                String message = getString(R.string.new_account_congratulations_text, email);
                Bundle bundle = new Bundle();
                bundle.putString("message", message);
                Intent intent = new Intent(WebViewInitActivity.this, PermissionInformationActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            } else {

                 new AlertDialog.Builder(WebViewInitActivity.this).setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).create().show();
            }
        }
    }
}
