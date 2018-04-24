package com.prey.activities.js;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.PanelWebActivity;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Created by oso on 21-11-17.
 */

public class WebAppInterface {

    Context mContext;
    int wrongPasswordIntents = 0;

    private CheckPasswordHtmlActivity mActivity;

    public WebAppInterface(Context context, CheckPasswordHtmlActivity activity) {
        mContext = context;


        mActivity=activity;
    }

    @JavascriptInterface
    public boolean openSettings(){
        PreyLogger.d("openSettings two:"+!PreyConfig.getPreyConfig(mContext).getTwoStep());
        return !PreyConfig.getPreyConfig(mContext).getTwoStep();
    }

    @JavascriptInterface
    public void signIn(String passwordtyped,String from) {
        PreyLogger.d("signIn:" + passwordtyped+" from:"+from);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CheckPassword().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,passwordtyped,from);
        else
            new CheckPassword().execute(passwordtyped,from);

    }


    @JavascriptInterface
    public void signInTwoStep(String passwordtyped,String passwordtyped2,String from) {
        PreyLogger.d("signIn:" + passwordtyped +" - "+ passwordtyped2+" "+from);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new CheckPassword().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,passwordtyped,passwordtyped2,from);
        else
            new CheckPassword().execute(passwordtyped,passwordtyped2,from);

    }

    @JavascriptInterface
    public void panel() {

        /*

        String url="https://panel.preyproject.com/login?email="+PreyConfig.getPreyConfig(mContext).getEmail()+"&id="+new Date().getTime();

        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW, Uri.parse(url));

        mContext.startActivity(browserIntent);
*/


        Intent intent  = new Intent(     mContext, PanelWebActivity.class);

        mContext.startActivity(intent);


    }

    @JavascriptInterface
    public boolean validarToken() {
        PreyLogger.d("validarToken no");
        return false;
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
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            final FrameLayout frameView = new FrameLayout(mContext);
            builder.setView(frameView);

            final AlertDialog alertDialog = builder.create();
            LayoutInflater inflater = alertDialog.getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.warning, frameView);


            Button button_ok = (Button) dialoglayout.findViewById(R.id.button_ok);
            Button button_close = (Button) dialoglayout.findViewById(R.id.button_close);


            button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreyLogger.d("askForPermission");
                    mActivity.askForPermission();
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
        }else{
            mActivity.askForPermissionAndroid7();
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

    public class CheckPassword extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;
        boolean isPasswordOk = false;
        String error = null;
        String from = "";

        @Override
        protected void onPreExecute() {
            /*try {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setMessage(mContext.getText(R.string.password_checking_dialog).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {

            }*/
        }

        @Override
        protected Void doInBackground(String... password) {
            try {
                isPasswordOk = false;
                String apikey = PreyConfig.getPreyConfig(mContext).getApiKey();
                if (PreyConfig.getPreyConfig(mContext).getTwoStep()) {
                    PreyLogger.d("apikey:" + apikey + " password:" + password[0] + " password2:" + password[1] + " from:" + password[2]);
                    from = password[2];
                    isPasswordOk =PreyWebServices.getInstance().checkPassword2(mContext,apikey, password[0],password[1]);
                }else{
                    PreyLogger.d("apikey:" + apikey + " password:" + password[0] +  " from:" + password[1]);
                    from = password[1];
                    isPasswordOk =PreyWebServices.getInstance().checkPassword(mContext,apikey, password[0]);
                }
                if(isPasswordOk) {
                    PreyConfig.getPreyConfig(mContext).setTimePasswordOk();
                }
            } catch (PreyException e) {
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
                Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
            else if (!isPasswordOk) {
                wrongPasswordIntents++;
                if (wrongPasswordIntents == 3) {
                    Toast.makeText(mContext, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(mContext, R.string.password_wrong, Toast.LENGTH_SHORT).show();
                }
            } else {
                if("panel".equals(from)) {
                    panel();
                }else{
                    Intent intent = new Intent(mContext, PreyConfigurationActivity.class);
                    PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
                    mContext.startActivity(intent);
                }
                new Thread(new EventManagerRunner(mContext, new Event(Event.APPLICATION_OPENED))).start();


            }
        }

    }
}
