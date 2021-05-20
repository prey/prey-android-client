/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.prey.PreyAccountData;
import com.prey.PreyApp;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;

public class WelcomeBatchActivity extends FragmentActivity {

    private String error = null;

    @Override
    public void onResume() {
        PreyLogger.d("onResume of WelcomeBatchActivity");
        super.onResume();
    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of WelcomeBatchActivity");
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
        menu();
        if(PreyConfig.getPreyConfig(this).isAskForNameBatch()){
            setContentView(R.layout.welcomebatch2);
            try {
                EditText editTextBatch2 = findViewById(R.id.editTextBatch2);
                editTextBatch2.setText(PreyUtils.getNameDevice(this));
            }catch (Exception e){
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            menu();
            Button buttonBatch2=(Button)findViewById(R.id.buttonBatch2);
            buttonBatch2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editTextBatch2=(EditText)findViewById(R.id.editTextBatch2);
                    String name=editTextBatch2.getText().toString();
                    if(name!=null&&!"".equals(name)) {
                        installBatch(name);
                    }else{
                        Toast.makeText(getApplicationContext(),getText(R.string.error),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            setContentView(R.layout.welcomebatch);
            menu();
            installBatch("");
        }
    }

    public void menu() {
        PreyLogger.d("menu ready:" + PreyConfig.getPreyConfig(this).getProtectReady());
        String email = PreyConfig.getPreyConfig(this).getEmail();
        if (email == null || "".equals(email)) {
            PreyConfig.getPreyConfig(this).setProtectReady(false);
            PreyConfig.getPreyConfig(this).setProtectAccount(false);
            PreyConfig.getPreyConfig(this).setProtectTour(false);
        }
    }

    private void installBatch(String name) {
        error=null;
        PreyConfig config=PreyConfig.getPreyConfig(this);
        new AddDeviceToApiKeyBatch().execute(config.getApiKeyBatch(),config.getEmailBatch(), PreyUtils.getDeviceType(this),name);
    }

    private class AddDeviceToApiKeyBatch extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(WelcomeBatchActivity.this);
                progressDialog.setMessage(WelcomeBatchActivity.this.getText(R.string.set_old_user_loading).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }catch (Exception e){
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                error = null;
                Context ctx=getApplicationContext();
                if(!PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey()) {
                    PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, data[0], data[1], data[2],data[3]);
                    PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                    PreyConfig.getPreyConfig(ctx).registerC2dm();
                    PreyConfig.getPreyConfig(ctx).setRunBackground(true);
                    RunBackgroundCheckBoxPreference.notifyReady(ctx);
                    new PreyApp().run(ctx);
                }
            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(progressDialog!=null)
                progressDialog.dismiss();
            if (error == null) {
                String message = getString(R.string.device_added_congratulations_text);
                Bundle bundle = new Bundle();
                bundle.putString("message", message);
                PreyConfig.getPreyConfig(WelcomeBatchActivity.this).setCamouflageSet(true);
                Intent intentPermission = new Intent(WelcomeBatchActivity.this, PermissionInformationActivity.class);
                intentPermission.putExtras(bundle);
                startActivity(intentPermission);
                finish();
            }
        }
    }

}