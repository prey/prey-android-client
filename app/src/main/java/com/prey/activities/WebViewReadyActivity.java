/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.javascript.WebAppInterface;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

import java.util.Locale;

public class WebViewReadyActivity extends Activity {

    private int wrongPasswordIntents = 0;
    @Override
    public void onResume() {
        PreyLogger.i("onResume of WebViewReadyActivity");
        super.onResume();
        init();
    }

    @Override
    public void onPause() {
        PreyLogger.i("onPause of WebViewReadyActivity");
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
        PreyLogger.i("onCreate of WebViewReadyActivity");



    }

    private void init(){
        PreyLogger.i("menu ready:" + PreyConfig.getPreyConfig(this).getProtectReady());
        if (!PreyConfig.getPreyConfig(this).getProtectReady()) {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
        WebView myWebView = (WebView) findViewById(R.id.install_browser);
        WebAppInterface webInterface=new WebAppInterface(this);
        webInterface.setActivityReady(this);

        myWebView.addJavascriptInterface(webInterface, "Android");
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String idioma="";
        if("es".equals(Locale.getDefault().getLanguage()))
            idioma="_es";
        if(PreyConfig.getPreyConfig(this).getProtectTour())
            myWebView.loadUrl("file:///android_asset/www/ready2"+idioma+".html");
        else
            myWebView.loadUrl("file:///android_asset/www/ready"+idioma+".html");
    }

    public void onBackPressed() {
        init();
    }

    public void checkPassword(String passwordtyped){
        new CheckPassword().execute(passwordtyped);
    }

    private class CheckPassword extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        boolean keepAsking = true;
        String error = null;


        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(WebViewReadyActivity.this);
                progressDialog.setMessage(WebViewReadyActivity.this.getText(R.string.password_checking_dialog).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                String email = PreyConfig.getPreyConfig(getApplicationContext()).getEmail();
                isPasswordOk = PreyWebServices.getInstance().checkPassword(WebViewReadyActivity.this, email, password[0]);


            } catch (PreyException e) {
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
                Toast.makeText(WebViewReadyActivity.this, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {
               /* boolean isAccountVerified = PreyConfig.getPreyConfig(getApplicationContext()).isAccountVerified();
                if (!isAccountVerified)
                    Toast.makeText(WebViewReadyActivity.this, R.string.verify_your_account_first, Toast.LENGTH_LONG).show();
                else {*/
                    wrongPasswordIntents++;
                    if (wrongPasswordIntents == 3) {
                        Toast.makeText(WebViewReadyActivity.this, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    } else {
                        Toast.makeText(WebViewReadyActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                    }
               // }
            } else {
                Intent intent = new Intent(WebViewReadyActivity.this, PreyConfigurationActivity.class);
                PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                startActivity(intent);
                new Thread(new EventManagerRunner(WebViewReadyActivity.this, new Event(Event.APPLICATION_OPENED))).start();
            }
        }

    }
}
