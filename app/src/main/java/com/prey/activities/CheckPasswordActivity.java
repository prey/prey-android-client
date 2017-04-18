/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;
import com.prey.services.PreyOverlayService;
import com.prey.util.Version;

public class CheckPasswordActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    int wrongPasswordIntents = 0;

    CheckPasswordActivity activity=null;
    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.password2);

        VersionTask versionTask=new VersionTask();
        versionTask.execute();
        activity=this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPasswordControls();
        TextView device_ready_h2_text=(TextView)findViewById(R.id.device_ready_h2_text);
        final  TextView textForgotPassword = (TextView) findViewById(R.id.link_forgot_password);

        Button password_btn_login=(Button)findViewById(R.id.password_btn_login);
        EditText password_pass_txt=(EditText)findViewById(R.id.password_pass_txt);

        TextView textView1=(TextView)findViewById(R.id.textView1);
        TextView textView2=(TextView)findViewById(R.id.textView2);


        Typeface titilliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Regular.ttf");
        Typeface titilliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/Titillium_Web/TitilliumWeb-Bold.ttf");
        Typeface magdacleanmonoRegular = Typeface.createFromAsset(getAssets(), "fonts/MagdaClean/magdacleanmono-regular.ttf");


        textView1.setTypeface(magdacleanmonoRegular);
        textView2.setTypeface(magdacleanmonoRegular);


        device_ready_h2_text.setTypeface(titilliumWebRegular);
        textForgotPassword.setTypeface(titilliumWebBold);
        password_btn_login.setTypeface(titilliumWebBold);
        password_pass_txt.setTypeface(magdacleanmonoRegular);

        try {

            textForgotPassword.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    try {
                        String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
                        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
        }



        TextView textView5_1=(TextView)findViewById(R.id.textView5_1);
        TextView textView5_2=(TextView)findViewById(R.id.textView5_2);

        textView5_1.setTypeface(magdacleanmonoRegular);
        textView5_2.setTypeface(titilliumWebBold);


        TextView textViewUninstall=(TextView) findViewById(R.id.textViewUninstall);
        LinearLayout linearLayoutTour = (LinearLayout) findViewById(R.id.linearLayoutTour);
        textViewUninstall.setTypeface(titilliumWebBold);


        if(PreyConfig.getPreyConfig(getApplication()).getProtectTour()) {
            linearLayoutTour.setVisibility(View.GONE);
            textViewUninstall.setVisibility(View.VISIBLE);

            textViewUninstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = PreyConfig.getPreyConfig(getApplication()).getPreyUninstallUrl();

                    Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    startActivity(browserIntent);

                    finish();
                }
            });
        }else{

            linearLayoutTour.setVisibility(View.VISIBLE);
            textViewUninstall.setVisibility(View.GONE);


            try {


                LinearLayout linearLayout3_1 = (LinearLayout) findViewById(R.id.linearLayout3_1);



                linearLayout3_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplication(), TourActivity1.class);
                        Bundle b = new Bundle();
                        b.putInt("id", 1);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    }
                });

                LinearLayout linearLayout3_2 = (LinearLayout) findViewById(R.id.linearLayout3_2);
                linearLayout3_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreyConfig.getPreyConfig(getApplication()).setProtectTour(true);
                        onResume();
                    }


                });
            }catch (Exception e){

            }
        }

        boolean showLocation=false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean canAccessFineLocation=PreyPermission.canAccessFineLocation(this);
            boolean canAccessCoarseLocation=PreyPermission.canAccessCoarseLocation(this);
            boolean canAccessCamera=PreyPermission.canAccessCamera(this);
            boolean canAccessReadPhoneState=PreyPermission.canAccessReadPhoneState(this);
            boolean canAccessReadExternalStorage=PreyPermission.canAccessReadExternalStorage(this);

            boolean canDrawOverlays=PreyPermission.canDrawOverlays(this);

            if(!canAccessFineLocation||!canAccessCoarseLocation||!canAccessCamera
                    || !canAccessReadPhoneState|| !canAccessReadExternalStorage){


                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final FrameLayout frameView = new FrameLayout(this);
                builder.setView(frameView);

                final AlertDialog alertDialog = builder.create();
                LayoutInflater inflater = alertDialog.getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.warning, frameView);

                TextView warning_title=(TextView)dialoglayout.findViewById(R.id.warning_title);
                TextView warning_body=(TextView)dialoglayout.findViewById(R.id.warning_body);

                warning_title.setTypeface(magdacleanmonoRegular);
                warning_body.setTypeface(titilliumWebBold);


                Button button_ok = (Button) dialoglayout.findViewById(R.id.button_ok);
                Button button_close = (Button) dialoglayout.findViewById(R.id.button_close);
                button_ok.setTypeface(titilliumWebBold);
                button_close.setTypeface(titilliumWebBold);

                final Activity thisActivity=this;
                button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreyLogger.d("askForPermission");
                        askForPermission();
                        alertDialog.dismiss();

                    }
                });

                button_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreyLogger.d("close ask");

                        alertDialog.dismiss();
                    }
                });



                alertDialog.show();
                showLocation=false;

            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!canDrawOverlays) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        final FrameLayout frameView = new FrameLayout(this);
                        builder.setView(frameView);

                        final AlertDialog alertDialog = builder.create();
                        LayoutInflater inflater = alertDialog.getLayoutInflater();
                        View dialoglayout = inflater.inflate(R.layout.warning_android7, frameView);

                        Button button_android7_ok = (Button) dialoglayout.findViewById(R.id.button_android7_ok);
                        Button button_android7_close = (Button) dialoglayout.findViewById(R.id.button_android7_close);

                        button_android7_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PreyLogger.d("askForPermissionAndroid7");
                                askForPermissionAndroid7();
                                startOverlayService();
                                alertDialog.dismiss();
                                finish();

                            }
                        });

                        alertDialog.show();
                        showLocation = false;
                    } else {
                        showLocation = true;
                    }
                } else {
                    showLocation = true;
                }
            }


        }else{
            showLocation=true;
        }
        if(showLocation) {
            LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGpsEnabled || isNetworkEnabled) {
                PreyLogger.d("isGpsEnabled || isNetworkEnabled");

            } else {
                PreyLogger.d("no gps ni red");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final AlertDialog alertDialog = builder.create();
                TextView textview = new TextView(this);
                textview.setText(getString(R.string.location_settings));
                textview.setMaxLines(10);
                textview.setTextSize(18F);
                textview.setPadding(20, 0, 20, 20);
                textview.setTextColor(Color.BLACK);
                builder.setView(textview);
                builder.setPositiveButton(getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0);
                        return;

                    }


                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                    }


                });
                builder.create().show();
            }
        }

        PreyConfig.getPreyConfig(this).registerC2dm();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PreyLogger.i("onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            PreyLogger.i("OVERLAY_PERMISSION_REQ_CODE");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissionAndroid7() {
        PreyLogger.i("askForPermissionAndroid7");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);

    }

    private void startOverlayService() {
        PreyLogger.i("startOverlayService");
        Intent intent = new Intent(getApplicationContext(), PreyOverlayService.class);
        startService(intent);
    }


    protected void bindPasswordControls() {
        Button checkPasswordOkButton = (Button) findViewById(R.id.password_btn_login);
        final EditText pass1 = ((EditText) findViewById(R.id.password_pass_txt));
        checkPasswordOkButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final String passwordtyped = pass1.getText().toString();
                final Context ctx = getApplicationContext();
                if (passwordtyped.equals(""))
                    Toast.makeText(ctx, R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
                else {
                    if (passwordtyped.length() < 6 || passwordtyped.length() > 32) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_password_out_of_range, "6", "32"), Toast.LENGTH_LONG).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            new CheckPassword().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,passwordtyped);
                        else
                            new CheckPassword().execute(passwordtyped);
                    }
                }

            }
        });

        //Hack to fix hint's typeface: http://stackoverflow.com/questions/3406534/password-hint-font-in-android
        EditText password = (EditText) findViewById(R.id.password_pass_txt);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
    }


    protected class CheckPassword extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        String error = null;


        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(CheckPasswordActivity.this);
                progressDialog.setMessage(CheckPasswordActivity.this.getText(R.string.password_checking_dialog).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                String apikey = PreyConfig.getPreyConfig(CheckPasswordActivity.this).getApiKey();
                PreyLogger.d("apikey:"+apikey+" password[0]:"+password[0]);
                isPasswordOk = PreyWebServices.getInstance().checkPassword(CheckPasswordActivity.this, apikey, password[0]);
                if(isPasswordOk) {
                    PreyConfig.getPreyConfig(CheckPasswordActivity.this).setTimePasswordOk();
                    PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_LOGIN_SETTINGS);
                } else {
                    PreyWebServices.getInstance().sendEvent(getApplication(), PreyConfig.ANDROID_FAILED_LOGIN_SETTINGS);
                }
            } catch (PreyException e) {
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
                Toast.makeText(CheckPasswordActivity.this, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {

                wrongPasswordIntents++;
                if (wrongPasswordIntents == 3) {
                    Toast.makeText(CheckPasswordActivity.this, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    Toast.makeText(CheckPasswordActivity.this, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                }

            } else {
                Intent intent = new Intent(CheckPasswordActivity.this, DeviceReadyActivity.class);
                PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                startActivity(intent);
                finish();
                new Thread(new EventManagerRunner(CheckPasswordActivity.this, new Event(Event.APPLICATION_OPENED))).start();
            }
        }

    }



    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermission() {
        ActivityCompat.requestPermissions(CheckPasswordActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }

    public void reset(){
        PreyLogger.i("reset");
        Intent intent = new Intent(CheckPasswordActivity.this, CheckPasswordActivity.class);
        startActivity(intent);
        finish();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PreyLogger.d("_______onRequestPermissionsResult_______requestCode:" + requestCode + " permissions:" + permissions.toString() + " grantResults:" + grantResults.toString());

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    PreyLogger.i("setCanAccessCamara");
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessCamara(true);
                }
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    PreyLogger.i("setCanAccessFineLocation");
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessFineLocation(true);
                }
                if (grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    PreyLogger.i("setCanAccessCoarseLocation");
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessCoarseLocation(true);
                }
                if (grantResults[3] ==  PackageManager.PERMISSION_GRANTED){
                    PreyLogger.i("setCanAccessReadPhoneState");
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessReadPhoneState(true);
                }
                if (grantResults[4] ==  PackageManager.PERMISSION_GRANTED){
                    PreyLogger.i("setCanAccessExternalStorage");
                    PreyConfig.getPreyConfig(getApplicationContext()).setCanAccessExternalStorage(true);
                }
                onResume();
                return;

            }
        }


    }

    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public String getVersionName() {
        String versionName =null;
        try{
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            versionName = info.versionName;
        }catch(Exception e){
        }
        return versionName;
    }

    public class VersionTask extends AsyncTask<Object, Void, Void> {




        private String googlePlayVersion="";
        @Override
        protected void onPreExecute() {
            googlePlayVersion="";
        }

        @Override
        protected Void doInBackground(Object... inputObj) {
            PreyLogger.d("VersionTask doInBackground");
            googlePlayVersion= PreyWebServices.getInstance().googlePlayVersion(getApplicationContext());
            PreyConfig.getPreyConfig(getApplication()).registerC2dm();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            PreyLogger.d("VersionTask onPostExecute");
            if(googlePlayVersion!=null){
                String versionName=getVersionName();
                PreyLogger.d("googlePlayVersion:"+googlePlayVersion+" versionName:"+versionName);
                Version versionGoggle = new Version(googlePlayVersion);
                Version versionPackage = new Version(versionName);

                if (versionGoggle.compareTo(versionPackage)==1) {
                    String title = getResources().getString(R.string.information);
                    String ccontinue = getString(R.string.ccontinue);
                    String download = getString(R.string.download);
                    String message = getResources().getString(R.string.new_version_available);
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    final AlertDialog dialog = builder.create();
                    dialog.setTitle(title);
                    dialog.setMessage(message);
                    dialog.setCancelable(false);
                    dialog.setButton(-1, ccontinue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setButton(-2,download, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                String uri = PreyConfig.getPreyConfig(getApplicationContext()).getPreyGooglePlay();
                                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(uri)));
                            }catch(Exception e){
                                PreyLogger.e("Error en onclick:"+e.getMessage(),e);
                            }
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        }
    }


}
