/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2022 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.prey.PreyBatch;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.net.PreyWebServices;

/****
 * This activity verify that the installer has a valid token
 */
public class SplashBatchActivity extends FragmentActivity {

    private String error = null;
    private TextView textSplash = null;

    @Override
    public void onResume() {
        PreyLogger.d("onResume of SplashBatchActivity");
        super.onResume();
        new SplashBatchActivity.TokenBatchTask().execute();
    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of SplashBatchActivity");
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreyLogger.d("onCreate of SplashBatchActivity");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash_batch);
        textSplash = (TextView) findViewById(R.id.text_splash);
    }

    /****
     * This asyncTask verify that the installer has a valid token
     */
    private class TokenBatchTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                textSplash.setText("");
                progressDialog = new ProgressDialog(SplashBatchActivity.this);
                progressDialog.setMessage(SplashBatchActivity.this.getText(R.string.loading).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {
                PreyLogger.e("Error:" + e.getMessage(), e);
            }
        }

        @Override
        protected Void doInBackground(String... data) {
            Context ctx = getApplicationContext();
            try {
                error = null;
                String token = PreyBatch.getInstance(ctx).getToken();
                if (token == null || "".equals(token)) {
                    error = ctx.getString(R.string.error_token);
                } else {
                    boolean validToken = PreyWebServices.getInstance().validToken(ctx, PreyBatch.getInstance(ctx).getToken());
                    if (!validToken) {
                        error = ctx.getString(R.string.error_token);
                    }
                }
            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (progressDialog != null)
                progressDialog.dismiss();
            if (error == null) {
                Intent intentPermission = new Intent(SplashBatchActivity.this, WelcomeBatchActivity.class);
                startActivity(intentPermission);
                finish();
            } else {
                textSplash.setText(error);
            }
        }

    }

}