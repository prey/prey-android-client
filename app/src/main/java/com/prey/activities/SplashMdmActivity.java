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
import com.prey.mdm.MdmDebugReporter;
import com.prey.mdm.MdmKeyedAppStateReporter;
import com.prey.receivers.RestrictionsReceiver;

import java.util.HashMap;
import java.util.Map;

public class SplashMdmActivity extends FragmentActivity {
    private static final String EXTRA_LAUNCHED_AS_SETUP_ACTION =
            "com.google.android.apps.work.clouddpc.EXTRA_LAUNCHED_AS_SETUP_ACTION";

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
        MdmDebugReporter.send(this, "splash_oncreate");
        new MdmRegistrationTask().execute();
    }

    private class MdmRegistrationTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Context ctx = getApplicationContext();
            try {
                publishProgress(getString(R.string.mdm_loading_title));
                RestrictionsManager manager = (RestrictionsManager) ctx.getSystemService(Context.RESTRICTIONS_SERVICE);
                Bundle restrictions = manager.getApplicationRestrictions();
                Map<String, Object> bgInfo = new HashMap<>();
                bgInfo.put("restrictions_null", restrictions == null);
                bgInfo.put("restrictions_empty", restrictions == null || restrictions.isEmpty());
                if (restrictions != null) {
                    bgInfo.put("restrictions_keys", restrictions.keySet().toString());
                    bgInfo.put("has_setup_key", restrictions.containsKey("setup_key"));
                }
                MdmDebugReporter.send(ctx, "splash_dobg_start", bgInfo);
                if (restrictions != null && !restrictions.isEmpty()) {
                    RestrictionsReceiver.handleApplicationRestrictions(ctx, restrictions);
                    boolean registered = PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey();
                    Map<String, Object> result = new HashMap<>();
                    result.put("registered", registered);
                    MdmDebugReporter.send(ctx, "splash_dobg_done", result);
                    return registered;
                }
            } catch (Exception e) {
                PreyLogger.e(String.format("Error SplashMdmActivity: %s", e.getMessage()), e);
                Map<String, Object> err = new HashMap<>();
                err.put("error", e.getMessage());
                MdmDebugReporter.send(ctx, "splash_dobg_error", err);
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values != null && values.length > 0 && textStatus != null) {
                textStatus.setText(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean registered) {
            Map<String, Object> postInfo = new HashMap<>();
            postInfo.put("registered", registered);
            postInfo.put("launched_as_setup_action", wasLaunchedAsSetupAction());
            postInfo.put("calling_activity_null", getCallingActivity() == null);
            MdmDebugReporter.send(getApplicationContext(), "splash_postexecute", postInfo);
            if (registered) {
                PreyConfig.getPreyConfig(getApplicationContext()).setProtectReady(true);
                // Emit the keyedAppState. setStatesImmediate is async (IPC to
                // clouddpc); if we finish() the activity right away, the
                // pipeline can drop the state mid-flight — AMAPI's webhook
                // ends up never receiving `mdm_setup=linked` even though Prey
                // successfully registered with the backend. Defer the
                // setResult+finish a bit so the IPC has time to flush.
                MdmDebugReporter.send(getApplicationContext(), "calling_reportSetupLinked");
                MdmKeyedAppStateReporter.reportSetupLinked(getApplicationContext());
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    setResult(RESULT_OK);
                    if (!wasLaunchedAsSetupAction() && getCallingActivity() == null) {
                        Intent intent = new Intent(SplashMdmActivity.this, CheckPasswordHtmlActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    MdmDebugReporter.send(getApplicationContext(), "splash_finishing_after_delay");
                    finish();
                }, 750);
            } else {
                setResult(RESULT_CANCELED);
                progressBar.setVisibility(View.GONE);
                textStatus.setText(R.string.mdm_loading_error);
            }
        }
    }

    private boolean wasLaunchedAsSetupAction() {
        Intent intent = getIntent();
        return intent != null && intent.getBooleanExtra(EXTRA_LAUNCHED_AS_SETUP_ACTION, false);
    }
}
