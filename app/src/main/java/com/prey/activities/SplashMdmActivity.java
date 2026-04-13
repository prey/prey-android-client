/*******************************************************************************
 * Created by Prey Inc.
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.receivers.RestrictionsReceiver;

public class SplashMdmActivity extends FragmentActivity {

    private TextView textStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splash_mdm);
        textStatus = (TextView) findViewById(R.id.text_mdm_status);
        progressBar = (ProgressBar) findViewById(R.id.progress_mdm);
        new MdmRegistrationTask().execute();
    }

    private class MdmRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Context ctx = getApplicationContext();
            try {
                RestrictionsManager manager = (RestrictionsManager) ctx.getSystemService(Context.RESTRICTIONS_SERVICE);
                Bundle restrictions = manager.getApplicationRestrictions();
                if (restrictions != null && !restrictions.isEmpty()) {
                    RestrictionsReceiver.handleApplicationRestrictions(ctx, restrictions);
                    return PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey();
                }
            } catch (Exception e) {
                PreyLogger.e(String.format("Error SplashMdmActivity: %s", e.getMessage()), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean registered) {
            if (registered) {
                Intent intent = new Intent(SplashMdmActivity.this, CheckPasswordHtmlActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                progressBar.setVisibility(View.GONE);
                textStatus.setText(R.string.mdm_loading_error);
            }
        }
    }
}
