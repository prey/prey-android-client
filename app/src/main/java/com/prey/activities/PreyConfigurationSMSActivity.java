/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;

public class PreyConfigurationSMSActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_sms);


        PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
        try {
            CheckBoxPreference pSMS= (CheckBoxPreference)findPreference("PREFS_SMS_COMMAND");
            PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
            PreyLogger.d("preyConfig.isSmsCommand:"+preyConfig.isSmsCommand());


            pSMS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    CheckBoxPreference pSMS = (CheckBoxPreference) findPreference("PREFS_SMS_COMMAND");
                    PreyLogger.d("preyConfig.newValue:" + newValue);
                    boolean value=((Boolean) newValue).booleanValue();
                    PreyConfig.getPreyConfig(getApplicationContext()).setSmsCommand(value);
                    pSMS.setChecked(value);
                    pSMS.setDefaultValue(value);
                    if(value){
                        requestPermission();;
                    }
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

    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!PreyPermission.canAccessSendSms(this)||!PreyPermission.canAccessReceiveSms(this)||!PreyPermission.canAccessReadSms(this)){
                ActivityCompat.requestPermissions(this, INITIAL_PERMS, REQUEST_PERMISSIONS);
            }
        }
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

    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    };




}

