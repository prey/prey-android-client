/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.actions.aware.AwareController;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.FingerprintAuthenticationDialogFragment;
import com.prey.activities.PanelWebActivity;
import com.prey.activities.PermissionInformationActivity;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.activities.SecurityActivity;
import com.prey.activities.SignInActivity;
import com.prey.activities.SignUpActivity;
import com.prey.activities.WelcomeActivity;
import com.prey.barcodereader.BarcodeActivity;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;

import java.util.HashMap;
import java.util.Map;

public class WebAppInterface2 {

    Context mContext;
    int wrongPasswordIntents = 0;

    private CheckPasswordHtmlActivity mActivity;

    public WebAppInterface2(Context context, CheckPasswordHtmlActivity activity) {
        mContext = context;
        mActivity=activity;
    }



    @JavascriptInterface
    public void oso(){
        PreyLogger.i("oso");
    }


    @JavascriptInterface
    public void mylogin(String email,String password){
        PreyLogger.i("mylogin llave:"+email+" valor:"+password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new AddDeviceToAccount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, email, password, PreyUtils.getDeviceType(mContext));
        else
            new AddDeviceToAccount().execute(email, password, PreyUtils.getDeviceType(mContext));
    }
    @JavascriptInterface
    public void login(String password){
        PreyLogger.i("login:"+ password);

        String passwordtyped2="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CheckPassword(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,password,passwordtyped2);
        else
            new CheckPassword(mContext).execute(password,passwordtyped2);

    }

    @JavascriptInterface
    public void login_tipo(String password,String tipo){
        PreyLogger.i("login_tipo:"+ password+" tipo:"+tipo);
        from=tipo;
        String passwordtyped2="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CheckPassword(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,password,passwordtyped2);
        else
            new CheckPassword(mContext).execute(password,passwordtyped2);

    }

    @JavascriptInterface
    public void qr(){
        PreyLogger.d("qr");
        Intent intent = new Intent(mContext, BarcodeActivity.class);
        mContext.startActivity(intent);
        mActivity.finish();
    }


    @JavascriptInterface
    public void signup(String name,String email,String password1,String password2,String policy_rule_age,String policy_rule_privacy_terms){
        PreyLogger.d("signup name: "+name+" email:"+email+" policy_rule_age:"+policy_rule_age+" policy_rule_privacy_terms:"+policy_rule_privacy_terms);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CreateAccount(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, name, email, password1,password2,policy_rule_age,policy_rule_privacy_terms);
        else
            new CreateAccount(mContext).execute(name, email, password1,password2,policy_rule_age,policy_rule_privacy_terms);
    }




    @JavascriptInterface
    public void givePermissions() {
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(mContext);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(mContext);
        boolean canAccessCamera = PreyPermission.canAccessCamera(mContext);
        boolean canAccessReadPhoneState = PreyPermission.canAccessReadPhoneState(mContext);
        boolean canAccessReadExternalStorage = PreyPermission.canAccessReadExternalStorage(mContext);
        PreyLogger.d("canAccessFineLocation:"+canAccessFineLocation);
        PreyLogger.d("canAccessCoarseLocation:"+canAccessCoarseLocation);
        PreyLogger.d("canAccessCamera:"+canAccessCamera);
        PreyLogger.d("canAccessReadPhoneState:"+canAccessReadPhoneState);
        PreyLogger.d("canAccessReadExternalStorage2:"+canAccessReadExternalStorage);
        boolean canDrawOverlays =false;
        //  boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
        //PreyLogger.d("canDrawOverlays:"+canDrawOverlays);
        if (!canAccessFineLocation || !canAccessCoarseLocation || !canAccessCamera
                || !canAccessReadPhoneState || !canAccessReadExternalStorage) {
            PreyLogger.d("dentro");

                    mActivity.askForPermission();

        }else{
            mActivity.askForPermissionAndroid7();
        }
    }



    private String error = null;
    private boolean noMoreDeviceError = false;

    private class AddDeviceToAccount extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setMessage(mContext.getText(R.string.set_old_user_loading).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }catch (Exception e){}
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                noMoreDeviceError = false;
                error = null;
                PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(mContext, data[0], data[1], data[2]);

                PreyConfig.getPreyConfig(mContext).saveAccount(accountData);
                PreyConfig.getPreyConfig(mContext).registerC2dm();
                PreyWebServices.getInstance().sendEvent(mContext, PreyConfig.ANDROID_SIGN_IN);
                String email=PreyWebServices.getInstance().getEmail(mContext);
                PreyConfig.getPreyConfig(mContext).setEmail(email);
                PreyConfig.getPreyConfig(mContext).setRunBackground(true);
                RunBackgroundCheckBoxPreference.notifyReady(mContext);
                new Thread() {
                    public void run() {
                        try{
                            PreyStatus.getInstance().getConfig(mContext);
                            AwareController.getInstance().init(mContext);
                        }catch (Exception e){}
                    }
                }.start();
                try {
                    Map<String, Object> eventValue = new HashMap<String, Object>();
                    eventValue.put(AFInAppEventParameterName.SUCCESS,true);
                    AppsFlyerLib.getInstance().trackEvent(mContext, AFInAppEventType.LOGIN,eventValue);
                } catch (Exception e1) {}
            } catch (Exception e) {
                PreyLogger.e("error:"+e.getMessage(),e);
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if(progressDialog!=null)
                    progressDialog.dismiss();
                if (error == null) {

                      mActivity.loadUrl();
                } else {
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder( mContext);
                    alertDialog.setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false);
                    alertDialog.show();
                }
            } catch (Exception e) {
            }
        }

    }


    String from="setting";

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
            }
            if (error != null)
                Toast.makeText(mCtx, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {
                wrongPasswordIntents++;
                if (wrongPasswordIntents == 3) {
                    Toast.makeText(mCtx, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mCtx, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                }
            } else {
                PreyLogger.d("from:"+from);
                if("setting".equals(from)) {
                    Intent intent = new Intent(mCtx, SecurityActivity.class);
                    PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mCtx.startActivity(intent);
                    new Thread(new EventManagerRunner(mCtx, new Event(Event.APPLICATION_OPENED))).start();
                    mActivity.finish();
                }else {
                    Intent intent = new Intent(mCtx, PanelWebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mCtx.startActivity(intent);
                    mActivity.finish();
                }
            }
        }
    }


    private class CreateAccount extends AsyncTask<String, Void, Void> {

        Context context;

        public CreateAccount(Context mContext) {
            this.context = mContext;
        }

        String email="";
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setMessage(mContext.getText(R.string.creating_account_please_wait).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }catch (Exception e) {}
            error = null;
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                error = null;
                final Context ctx=mContext;
                String name=data[0];
                email=data[1];
                String password1=data[2];
                String password2=data[3];
                String rule_age=data[4];
                String privacy_terms=data[5];

                PreyLogger.d("name:"+name);
                PreyLogger.d("email:"+email);
                PreyLogger.d("password1:"+password1);
                PreyLogger.d("password2:"+password2);
                PreyLogger.d("rule_age:"+rule_age);
                PreyLogger.d("privacy_terms:"+privacy_terms);



                PreyAccountData accountData = PreyWebServices.getInstance().registerNewAccount(ctx, name, email, password1,password2,rule_age,privacy_terms,PreyUtils.getDeviceType(mContext));
                PreyLogger.d("Response creating account: " + accountData.toString());
                PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                PreyConfig.getPreyConfig(ctx).registerC2dm();
                PreyWebServices.getInstance().sendEvent(ctx,PreyConfig.ANDROID_SIGN_UP);
                PreyConfig.getPreyConfig(ctx).setEmail(email);
                PreyConfig.getPreyConfig(ctx).setRunBackground(true);
                RunBackgroundCheckBoxPreference.notifyReady(ctx);
                new Thread() {
                    public void run() {
                        try {
                            PreyStatus.getInstance().getConfig(mContext);
                            AwareController.getInstance().init(ctx);
                        }catch (Exception e){}
                    }
                }.start();
                try {
                    Map<String, Object> eventValue = new HashMap<>();
                    eventValue.put(AFInAppEventParameterName.REGSITRATION_METHOD,"email");
                    AppsFlyerLib.getInstance().trackEvent(ctx, AFInAppEventType.COMPLETE_REGISTRATION,eventValue);
                } catch (Exception e1) {}
            } catch (Exception e) {
                error = e.getMessage();
                PreyLogger.e("e.getMessage():"+e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if(progressDialog!=null)
                    progressDialog.dismiss();

                if (error == null) {
                    mActivity.loadUrl();
                } else {
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder( mContext);
                    alertDialog.setIcon(R.drawable.error).setTitle(R.string.error_title).setMessage(error)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false);
                    alertDialog.show();
                }
            } catch (Exception e) {
            }
        }
    }
}
