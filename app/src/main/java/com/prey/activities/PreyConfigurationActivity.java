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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.prey.PreyConfig;
import com.prey.PreyEmail;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.R;
import com.prey.preferences.ChangePinPreferences;

public class PreyConfigurationActivity extends PreferenceActivity {

    public void onBackPressed(){
        Intent intent = null;

        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);

        startActivity(intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
        preyConfig.setAccountVerified();
        addPreferencesFromResource(R.xml.preferences);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO Auto-generated method stub
        if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            try {
                startActivity(intent);
            } catch (Exception e) {
            }
            finish();

        }
        try {
            GoogleApiAvailability api = GoogleApiAvailability.getInstance();
            int code = api.isGooglePlayServicesAvailable(getApplicationContext());
            if (code == ConnectionResult.SUCCESS) {
                PreferenceCategory mCategory = (PreferenceCategory) findPreference("PREFS_CAT_PREFS");
                Preference p2 = findPreference("PREFS_SCHEDULED");
                p2.setEnabled(false);
                mCategory.removePreference(p2);
            }
        } catch (Exception e) {
        }

        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());

        CheckBoxPreference pDisablePower=(CheckBoxPreference)findPreference("PREFS_DISABLE_POWER");
        try {
            if (preyConfig.isMarshmallowOrAbove()) {
                pDisablePower.setSummary(R.string.preferences_disable_power_options_summary);
            } else {
                pDisablePower.setSummary(R.string.preferences_disable_power_options_summary_old);
            }
        }catch(Exception e){
        }

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
                }
                return false;
            }
        });



        Preference pSMS= findPreference("PREFS_SMS");
        pSMS.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(getApplicationContext(), PreyConfigurationSMSActivity.class);
                startActivity(intent);

                return false;
            }
        });

        CheckBoxPreference pBlockAppUninstall=(CheckBoxPreference)findPreference(PreyConfig.PREFS_BLOCK_APP_UNINSTALL);

        try {
            if ("".equals(preyConfig.getPinNumber())) {
                pSMS.setEnabled(false);
                pDisablePower.setEnabled(false);
                PreyConfig.getPreyConfig(getApplicationContext()).setDisablePowerOptions(false);
                pDisablePower.setChecked(false);
                pBlockAppUninstall.setEnabled(false);
                PreyConfig.getPreyConfig(getApplicationContext()).setBlockAppUninstall(false);
                pBlockAppUninstall.setChecked(false);
            }else{
                pSMS.setEnabled(true);
                pDisablePower.setEnabled(true);
                pBlockAppUninstall.setEnabled(true);
            }
        } catch (Exception e) {
        }
        PreyStatus.getInstance().setPreyConfigurationActivityResume(false);



        ChangePinPreferences changePin=(ChangePinPreferences)findPreference("PREFS_CHANGE_PIN");
        String pin=PreyConfig.getPreyConfig(this).getPinNumber();
        if("".equals(pin)) {
            changePin.setPositiveButtonText(R.string.preference_pin_save);
        }else{
            changePin.setPositiveButtonText(R.string.preference_pin_remove);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


}

