/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.net.PreyWebServices;

public class FingerprintAuthenticationDialogFragment  extends DialogFragment   implements FingerprintHelper.FingerprintHelperListener{

    private Stage mStage = Stage.FINGERPRINT;
    private View mFingerprintContent;
    private View mBackupContent;
    private Button mSecondDialogButton;
    private Button mFirstDialogButton;
    private EditText mPassword;
    private EditText mPassword2;
    private TextView password_description;
    private TextView password_description2;
    private TextView fingerprint_description;
    private String from="";
    private FingerprintHelper mFingerprintUiHelper = null;
    private FingerprintManager fingerprintManager = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        Bundle bundle=getArguments();
        from=bundle.getString("from");
        PreyLogger.d("from:"+from);
        mFingerprintUiHelper=null;
        mStage = Stage.PASSWORD;
        mFirstDialogButton = (Button) v.findViewById(R.id.first_dialog_button);
        mFirstDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStage == Stage.FINGERPRINT) {
                    mStage = Stage.PASSWORD;
                    updateStage();
                } else {
                    mStage = Stage.FINGERPRINT;
                    updateStage();
                }
            }
        });
        mSecondDialogButton = (Button) v.findViewById(R.id.second_dialog_button);
        mSecondDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStage == Stage.FINGERPRINT) {
                    goToBackup();
                } else {
                    verifyPassword();
                }
            }
        });
        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mBackupContent = v.findViewById(R.id.backup_container);
        mPassword = (EditText) v.findViewById(R.id.password);
        mPassword2 = (EditText) v.findViewById(R.id.password2);
        password_description = (TextView) v.findViewById(R.id.password_description);
        password_description2 = (TextView) v.findViewById(R.id.password_description2);
        fingerprint_description = (TextView) v.findViewById(R.id.fingerprint_description);
        Typeface regularBold= Typeface.createFromAsset(getActivity().getAssets(), "fonts/Regular/regular-bold.otf");
        password_description.setTypeface(regularBold);
        password_description2.setTypeface(regularBold);
        mPassword.setTypeface(regularBold);
        mPassword2.setTypeface(regularBold);
        mSecondDialogButton.setTypeface(regularBold);
        mFirstDialogButton.setTypeface(regularBold);
        fingerprint_description.setTypeface(regularBold);
        updateStage();
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        PreyLogger.d("onPause");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(mFingerprintUiHelper!=null) {
                mFingerprintUiHelper.cancel();
            }
        }
        mFingerprintUiHelper=null;
    }

    @Override
    public void authenticationFailed(String error) {
        PreyLogger.d("authenticationFailed");
        if(mFingerprintUiHelper!=null) {
            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void authenticationSuccess(FingerprintManager.AuthenticationResult result) {
        PreyLogger.d("authenticationSuccess");
        boolean prefsBiometric=PreyConfig.getPreyConfig(getActivity()).getPrefsBiometric();
        if(prefsBiometric) {
            PreyConfig.getPreyConfig(getActivity()).setTimePasswordOk();
            Intent intent = new Intent(getActivity(), PreyConfigurationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
            getActivity().startActivity(intent);
            getActivity().finish();
        }else{
            Toast.makeText(getActivity(),R.string.biometric_prompt_error,Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mFingerprintUiHelper = new FingerprintHelper(this);
                fingerprintManager = (FingerprintManager) getActivity().getSystemService(getActivity().FINGERPRINT_SERVICE);
                mFingerprintUiHelper.startAuth(fingerprintManager, null);
            }
        }
    }

    private void verifyPassword() {
        String passwordtyped=mPassword.getText().toString();
        String passwordtyped2=mPassword2.getText().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CheckPassword(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,passwordtyped,passwordtyped2);
        else
            new CheckPassword(getActivity()).execute(passwordtyped,passwordtyped2);
    }

    private void goToBackup() {
        mStage = Stage.PASSWORD;
        updateStage();
        mPassword.requestFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(mFingerprintUiHelper!=null) {
                mFingerprintUiHelper.cancel();
            }
        }
        mFingerprintUiHelper=null;
    }

    private void updateStage() {
        PreyLogger.d("updateStage:"+(mStage==Stage.FINGERPRINT?"FINGERPRINT":"PASSWORD"));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        switch (mStage) {
            case FINGERPRINT:
                mFirstDialogButton.setVisibility(View.GONE);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                mBackupContent.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mFingerprintUiHelper = new FingerprintHelper(this);
                    fingerprintManager = (FingerprintManager) getActivity().getSystemService(getActivity().FINGERPRINT_SERVICE);
                    mFingerprintUiHelper.startAuth(fingerprintManager, null);
                    PreyLogger.d("mFingerprintUiHelper startAuth");
                }
                break;
            case PASSWORD:
                boolean checkBiometricSupport=  PreyPermission.checkBiometricSupport(getActivity());
                if(!checkBiometricSupport){
                    mFirstDialogButton.setVisibility(View.GONE);
                }else{
                    mFirstDialogButton.setVisibility(View.VISIBLE);
                }
                mFirstDialogButton.setText(R.string.use_fingerprint);
                mSecondDialogButton.setText(R.string.ok);
                mFingerprintContent.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                boolean openSettingsTwoStep=PreyConfig.getPreyConfig(getActivity()).getTwoStep();
                PreyLogger.d("openSettingsTwoStep:"+openSettingsTwoStep);
                if(openSettingsTwoStep) {
                    password_description2.setVisibility(View.VISIBLE);
                    mPassword2.setVisibility(View.VISIBLE);
                }else{
                    password_description2.setVisibility(View.GONE);
                    mPassword2.setVisibility(View.GONE);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(mFingerprintUiHelper!=null) {
                        mFingerprintUiHelper.cancel();
                    }
                }
                break;
        }
    }

    public enum Stage {
        FINGERPRINT,
        PASSWORD
    }

    int wrongPasswordIntents = 0;

    public class CheckPassword extends AsyncTask<String, Void, Void> {
        private Context mCtx;
        CheckPassword(Context ctx){
            mCtx =ctx;
        }
        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        String error = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                isPasswordOk = false;
                String apikey = PreyConfig.getPreyConfig(mCtx).getApiKey();
                boolean twoStep=PreyConfig.getPreyConfig(mCtx).getTwoStep();
                PreyLogger.d("twoStep:" +twoStep);
                if (twoStep) {
                    PreyLogger.d("apikey:" + apikey + " password:" + password[0] + " password2:" + password[1] );
                    isPasswordOk =PreyWebServices.getInstance().checkPassword2(mCtx,apikey, password[0],password[1]);
                }else{
                    PreyLogger.d("apikey:" + apikey + " password:" + password[0] );
                    isPasswordOk =PreyWebServices.getInstance().checkPassword(mCtx,apikey, password[0]);
                }
                if(isPasswordOk) {
                    PreyConfig.getPreyConfig(mCtx).setTimePasswordOk();
                }
            } catch (Exception e) {
                PreyLogger.e("error:"+e.getMessage(),e);
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
                PreyLogger.e("Error:"+e.getMessage(),e);
            }
            if (error != null)
                Toast.makeText(mCtx, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {
                wrongPasswordIntents++;
                if (wrongPasswordIntents == 3) {
                    Toast.makeText(mCtx, R.string.password_intents_exceed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mCtx, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                }
            } else {
                PreyLogger.d("from:"+from);
                if("setting".equals(from)) {
                    Intent intent = new Intent(mCtx, PreyConfigurationActivity.class);
                    PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mCtx.startActivity(intent);
                    new Thread(new EventManagerRunner(mCtx, new Event(Event.APPLICATION_OPENED))).start();
                    dismiss();
                }else {
                    Intent intent = new Intent(mCtx, PanelWebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mCtx.startActivity(intent);
                    dismiss();
                }
            }
        }
    }

}