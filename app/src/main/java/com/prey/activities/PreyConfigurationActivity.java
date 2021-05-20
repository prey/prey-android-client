/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.R;

public class PreyConfigurationActivity extends PreferenceActivity {

    public void onBackPressed(){
        Intent intent = null;
        intent = new Intent(getApplication(), CheckPasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
        preyConfig.setAccountVerified();
        addPreferencesFromResource(R.xml.preferences_5);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            try {
                startActivity(intent);
            } catch (Exception e) {
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            finish();
        }
        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
        Preference p = findPreference("PREFS_ADMIN_DEVICE");
        try {
            if (preyConfig.isFroyoOrAbove()) {
                if (FroyoSupport.getInstance(getApplicationContext()).isAdminActive()) {
                    p.setTitle(R.string.preferences_admin_enabled_title);
                    p.setSummary(R.string.preferences_admin_enabled_summary);
                } else {
                    p.setTitle(R.string.preferences_admin_disabled_title);
                    p.setSummary(R.string.preferences_admin_disabled_summary);
                }
            } else
                p.setEnabled(false);
        }catch(Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        p = findPreference("PREFS_ABOUT");
        p.setSummary("Version " + preyConfig.getPreyVersion() + " - Prey Inc.");
        Preference pGo = findPreference("PREFS_GOTO_WEB_CONTROL_PANEL");
        pGo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
                PreyLogger.d("url control:" + url);
                Intent internetIntent = new Intent(Intent.ACTION_VIEW);
                internetIntent.setData(Uri.parse(url));
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    PreyLogger.e("Error:"+e.getMessage(),e);
                }
                return false;
            }
        });
        PreyStatus.getInstance().setPreyConfigurationActivityResume(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}