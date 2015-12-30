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

import com.prey.PreyConfig;
import com.prey.PreyEmail;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.backwardcompatibility.FroyoSupport;

public class PreyConfigurationSMSActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_sms);


        PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
        try {
            CheckBoxPreference pSMS= (CheckBoxPreference)findPreference("PREFS_SMS_COMMAND");
            PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
            PreyLogger.i("preyConfig.isSmsCommand:"+preyConfig.isSmsCommand());


            pSMS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    CheckBoxPreference pSMS= (CheckBoxPreference)findPreference("PREFS_SMS_COMMAND");
                    PreyLogger.i("preyConfig.newValue:"+newValue);
                    PreyConfig.getPreyConfig(getApplicationContext()).setSmsCommand((Boolean) newValue);
                    pSMS.setChecked((Boolean) newValue);
                    pSMS.setDefaultValue((Boolean) newValue);
                    return false;
                }
            });

            if (!preyConfig.isSmsCommand()) {
                pSMS.setChecked(false);
                pSMS.setDefaultValue(false);
            }else{
                pSMS.setChecked(true);
                pSMS.setDefaultValue(true);
            }

        } catch (Exception e) {
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();





    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), PreyConfigurationActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();

    }




}

