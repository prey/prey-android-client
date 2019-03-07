/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.prey.FileConfigReader;
import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.actions.aware.AwareController;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;
import com.prey.util.HttpUtil;
import com.prey.util.KeyboardStatusDetector;
import com.prey.util.KeyboardVisibilityListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUpActivity extends Activity {

    private static final int ERROR = 1;
    private String error = null;
    private String email = null;
    private String htmTerms="";

    public void onResume() {
        PreyLogger.d("onResume of SignUpActivity");
        super.onResume();

    }

    @Override
    public void onPause() {
        PreyLogger.d("onPause of SignUpActivity");
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
        this.setContentView(R.layout.signup);
        PreyLogger.d("onCreate of SignUpActivity");

        final EditText nameText=((EditText)findViewById(R.id.editTextName));
        final EditText emailText=((EditText)findViewById(R.id.editTextEmailAddress));
        final EditText passwordText=((EditText)findViewById(R.id.editTextPassword));

        Button buttonSignup = (Button) findViewById(R.id.buttonSignup);

        final TextView linkSignup = (TextView) findViewById(R.id.linkSignup);
        final CheckBox checkBox_linear_agree_terms_condition=(CheckBox)findViewById(R.id.checkBox_linear_agree_terms_condition);
        final CheckBox checkBox_linear_confirm_over=(CheckBox)findViewById(R.id.checkBox_linear_confirm_over);

        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");

        TextView textViewInit1=(TextView)findViewById(R.id.textViewInit1);
        TextView textViewInit2=(TextView)findViewById(R.id.textViewInit2);
        TextView text_linear_agree_terms_condition=(TextView)findViewById(R.id.text_linear_agree_terms_condition);
        TextView text_linear_confirm_over=(TextView)findViewById(R.id.text_linear_confirm_over);

        textViewInit1.setTypeface(magdacleanmonoRegular);
        textViewInit2.setTypeface(titilliumWebBold);
        text_linear_agree_terms_condition.setTypeface(titilliumWebBold);
        text_linear_confirm_over.setTypeface(titilliumWebBold);

        final Context ctx=this;

        String urlTerms="";
        if ("es".equals(Locale.getDefault().getLanguage())) {
            urlTerms=FileConfigReader.getInstance(getApplicationContext()).getPreyTermsEs();
        }else{
            urlTerms=FileConfigReader.getInstance(getApplicationContext()).getPreyTerms();
        }
        PreyLogger.d("urlTerms:"+urlTerms);
        htmTerms= HttpUtil.getContents(urlTerms);
        String regex = "<a href.*?>";
        htmTerms = htmTerms.replaceAll(regex, "<a>");

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        final AlertDialog alert =builder.create();
        WebView wv = new WebView(getApplicationContext());
        wv.setWebChromeClient(new WebChromeClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(false);
        wv.getSettings().setSupportZoom(false);
        wv.loadData(htmTerms, "text/html", "UTF-8");
        alert.setView(wv);

        alert.setButton(getString(R.string.warning_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                try{
                    dialog.dismiss();
                }catch(Exception e){};
            }
        });

        text_linear_agree_terms_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    alert.show();
                }catch(Exception e){};
            }
        });

        linkSignup.setTypeface(titilliumWebBold);
        buttonSignup.setTypeface(titilliumWebBold);

        nameText.setTypeface(magdacleanmonoRegular);
        emailText.setTypeface(magdacleanmonoRegular);
        passwordText.setTypeface(magdacleanmonoRegular);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final int halfHeight=metrics.heightPixels/3;
        KeyboardStatusDetector keyboard = new KeyboardStatusDetector();

        keyboard.registerActivity(this);


        buttonSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString();
                email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                String confirm_over=""+checkBox_linear_confirm_over.isChecked();
                String agree_terms_condition=""+checkBox_linear_agree_terms_condition.isChecked();
                PreyLogger.d("email:"+email);
                PreyLogger.d("password:"+password);
                PreyLogger.d("confirm_over:"+confirm_over);
                PreyLogger.d("agree_terms_condition:"+agree_terms_condition);
                Context ctx = getApplicationContext();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                new CreateAccount(ctx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, name, email, password,confirm_over,agree_terms_condition);
                            else
                                new CreateAccount(ctx).execute(name, email, password,confirm_over,agree_terms_condition);
            }
        });


        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), InitActivity.class);
        startActivity(intent);
        finish();
    }


    private class CreateAccount extends AsyncTask<String, Void, Void> {

        Context context;

        public CreateAccount(Context mContext) {
            this.context = mContext;
        }


        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(SignUpActivity.this);
                progressDialog.setMessage(SignUpActivity.this.getText(R.string.creating_account_please_wait).toString());
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
                final Context ctx=getApplicationContext();
                PreyAccountData accountData = PreyWebServices.getInstance().registerNewAccount(ctx, data[0], data[1], data[2],data[3],data[4],PreyUtils.getDeviceType(getApplication()));
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
                            PreyStatus.getInstance().getConfig(getApplicationContext());
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
                    String message = getString(R.string.new_account_congratulations_text, email);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", message);
                    Intent intent =null;
                    if (PreyConfig.getPreyConfig(SignUpActivity.this).isChromebook()) {
                        intent = new Intent(SignUpActivity.this, WelcomeActivity.class);
                        PreyConfig.getPreyConfig(SignUpActivity.this).setProtectReady(true);
                    }else {
                        intent = new Intent(SignUpActivity.this, PermissionInformationActivity.class);
                    }
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } else {
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder( SignUpActivity.this);
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
