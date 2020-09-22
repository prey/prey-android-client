/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.prey.FileConfigReader;
import com.prey.PreyAccountData;
import com.prey.PreyApp;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyUtils;
import com.prey.PreyVerify;
import com.prey.R;
import com.prey.actions.location.LocationUpdatesService;
import com.prey.actions.location.PreyLocation;
import com.prey.activities.CheckPasswordHtmlActivity;
import com.prey.activities.LoginActivity;
import com.prey.activities.PanelWebActivity;
import com.prey.activities.PreReportActivity;
import com.prey.activities.SecurityActivity;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.barcodereader.BarcodeActivity;
import com.prey.exceptions.PreyException;
import com.prey.json.UtilJson;
import com.prey.json.actions.Detach;
import com.prey.net.PreyWebServices;
import com.prey.preferences.RunBackgroundCheckBoxPreference;
import com.prey.services.PreyDisablePowerOptionsService;
import com.prey.services.PreyJobService;

import org.json.JSONObject;

import java.net.HttpURLConnection;


public class WebAppInterface {

    public Context mContext;
    private int wrongPasswordIntents = 0;
    private String error = null;
    private boolean noMoreDeviceError = false;
    private String from = "setting";
    private CheckPasswordHtmlActivity mActivity;

    public WebAppInterface() {
    }

    public WebAppInterface(Context ctx) {
        mContext = ctx;
    }

    public WebAppInterface(Context context, CheckPasswordHtmlActivity activity) {
        mContext = context;
        mActivity = activity;
    }

    @JavascriptInterface
    public String getData() {
        String ssid = PreyConfig.getPreyConfig(mContext).getSsid();
        String model = PreyConfig.getPreyConfig(mContext).getModel();
        String imei = PreyConfig.getPreyConfig(mContext).getImei();
        PreyLocation preyLocation = PreyConfig.getPreyConfig(mContext).getLocation();
        String lat = "" + LocationUpdatesService.round(preyLocation.getLat());
        String lng = "" + LocationUpdatesService.round(preyLocation.getLng());
        String public_ip = PreyConfig.getPreyConfig(mContext).getPublicIp().trim();
        String json = "{\"lat\":\"" + lat + "\",\"lng\":\"" + lng + "\",\"ssid\":\"" + ssid + "\",\"public_ip\":\"" + public_ip + "\",\"imei\":\"" + imei + "\",\"model\": \"" + model + "\"}";
        PreyLogger.d("getData:" + json);
        return json;
    }

    @JavascriptInterface
    public boolean initBackground() {
        boolean initBackground = PreyConfig.getPreyConfig(mContext).getRunBackground();
        PreyLogger.d("initBackground:" + initBackground);
        return initBackground;
    }

    @JavascriptInterface
    public boolean initPin() {
        String pinNumber = PreyConfig.getPreyConfig(mContext).getPinNumber();
        boolean initPin = (pinNumber != null && !"".equals(pinNumber));
        PreyLogger.d("initPin:" + initPin);
        return initPin;
    }

    @JavascriptInterface
    public boolean initAdminActive() {
        return FroyoSupport.getInstance(mContext).isAdminActive();
    }

    @JavascriptInterface
    public boolean initDrawOverlay() {
        boolean canDrawOverlays = true;
        if(PreyConfig.getPreyConfig(mContext).isOverOtherApps()){
            canDrawOverlays=PreyPermission.canDrawOverlays(mContext);
        }
        return canDrawOverlays;
    }

    @JavascriptInterface
    public boolean initAccessibility() {
        return  PreyPermission.isAccessibilityServiceEnabled(mContext);
    }

    @JavascriptInterface
    public boolean initLocation() {
        return (PreyPermission.canAccessFineLocation(mContext)||PreyPermission.canAccessCoarseLocation(mContext));
    }

    @JavascriptInterface
    public boolean initBackgroundLocation() {
        return PreyPermission.canAccessBackgroundLocation(mContext);
    }

    @JavascriptInterface
    public boolean initAndroid10OrAbove() {
        return PreyConfig.getPreyConfig(mContext).isAndroid10OrAbove();
    }

    @JavascriptInterface
    public boolean initCamera() {
        return PreyPermission.canAccessCamera(mContext);
    }

    @JavascriptInterface
    public boolean initReadPhone() {
        return PreyPermission.canAccessPhone(mContext);
    }

    @JavascriptInterface
    public boolean initWriteStorage() {
        return PreyPermission.canAccessStorage(mContext);
    }

    @JavascriptInterface
    public String getPin() {
        String pin = PreyConfig.getPreyConfig(mContext).getPinNumber();
        PreyLogger.d("getPin:" + pin);
        return pin;
    }

    @JavascriptInterface
    public boolean initUninstall() {
        boolean initUnis = PreyConfig.getPreyConfig(mContext).getBlockAppUninstall();
        PreyLogger.d("initUninstall:" + initUnis);
        return initUnis;
    }

    @JavascriptInterface
    public boolean initShield() {
        boolean initShi = PreyConfig.getPreyConfig(mContext).getDisablePowerOptions();
        PreyLogger.d("initShield:" + initShi);
        return initShi;
    }

    @JavascriptInterface
    public String initScheduler() {
        int initSche = PreyConfig.getPreyConfig(mContext).getMinuteScheduled();
        return ""+initSche;
    }

    @JavascriptInterface
    public void changeScheduler(String minuteScheduled) {
        PreyLogger.d("changeScheduler:" + minuteScheduled);
        PreyConfig.getPreyConfig(mContext).setMinuteScheduled(Integer.parseInt(minuteScheduled));
        PreyJobService.schedule(mContext);
    }

    @JavascriptInterface
    public void report() {
        PreyLogger.d("report:");
        Intent intent = new Intent(mContext, PreReportActivity.class);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    @JavascriptInterface
    public void security() {
        PreyLogger.d("security:");
        Intent intent = new Intent(mContext, SecurityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    @JavascriptInterface
    public void reload() {
        PreyLogger.d("reload:");
        Intent intent = new Intent(mContext, CheckPasswordHtmlActivity.class);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    @JavascriptInterface
    public void savePin(String pin) {
        PreyLogger.d("savepin:" + pin);
        PreyConfig.getPreyConfig(mContext).setPinNumber(pin);
    }

    @JavascriptInterface
    public void log(String log) {
        PreyLogger.d("log:" + log);
    }


    @JavascriptInterface
    public String mylogin(final String email,final String password) {
        PreyLogger.d("mylogin email:" + email + " password:" + password);

        try {
            noMoreDeviceError = false;
            error = null;
            final Context ctx=mContext;
            PreyConfig.getPreyConfig(mContext).setError(null);
            String errorConfig=null;
            new Thread() {
                public void run() {
                    try {
                        PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(ctx, email, password, PreyUtils.getDeviceType(ctx));
                        PreyConfig.getPreyConfig(mContext).saveAccount(accountData);
                    } catch (Exception e) {
                        PreyLogger.d("mylogin error2:" + e.getMessage());
                        PreyConfig.getPreyConfig(mContext).setError(e.getMessage());
                    }
                }
            }.start();
            boolean isAccount=false;
            int i=0;
            do{
                Thread.sleep(1000);
                errorConfig= PreyConfig.getPreyConfig(mContext).getError();
                isAccount=PreyConfig.getPreyConfig(mContext).isAccount();
                PreyLogger.d("mylogin ["+i+"] isAccount:" +isAccount);
                i++;
            }while(i<30&&!isAccount&&errorConfig==null);
            isAccount=PreyConfig.getPreyConfig(mContext).isAccount();
            if(!isAccount){
                if(errorConfig!=null&&!"".equals(errorConfig)){
                    error=errorConfig;
                }else {
                    error = "{\"error\":[\"" + ctx.getText(R.string.error_communication_exception).toString() + "\"]}";
                }
            }else {
                PreyConfig.getPreyConfig(mContext).registerC2dm();
                PreyWebServices.getInstance().sendEvent(mContext, PreyConfig.ANDROID_SIGN_IN);
                PreyConfig.getPreyConfig(mContext).setEmail(email);
                PreyConfig.getPreyConfig(mContext).setRunBackground(true);
                RunBackgroundCheckBoxPreference.notifyReady(mContext);
                PreyConfig.getPreyConfig(mContext).setInstallationStatus("");
                new PreyApp().run(mContext);
            }
        } catch (Exception e) {
            PreyLogger.d("mylogin error1:" + e.getMessage());
            error = e.getMessage();
        }

        if (error == null) {
            error = "";
        }
        PreyLogger.d("mylogin error2:" + error);
        return error;
    }

    @JavascriptInterface
    public boolean isTimePasswordOk() {
        boolean isTimePasswordOk = PreyConfig.getPreyConfig(mContext).isTimePasswordOk();
        PreyLogger.d("isTimePasswordOk:" + isTimePasswordOk);
        return isTimePasswordOk;
    }

    @JavascriptInterface
    public String login_tipo(String password, String password2, String tipo) {
        PreyLogger.d("login_tipo2 password:" + password + " password2:" + password2 + " tipo:" + tipo);
        from = tipo;
        error = null;
        boolean isPasswordOk = false;

        try {
            String apikey = PreyConfig.getPreyConfig(mContext).getApiKey();
            boolean twoStep = PreyConfig.getPreyConfig(mContext).getTwoStep();
            PreyLogger.d("login_tipo twoStep:" + twoStep);
            if (twoStep) {
                PreyLogger.d("login_tipo apikey:" + apikey + " password:" + password + " password2:" + password2);
                isPasswordOk = PreyWebServices.getInstance().checkPassword2(mContext, apikey, password, password2);
            } else {
                PreyLogger.d("login_tipo apikey:" + apikey + " password:" + password);
                isPasswordOk = PreyWebServices.getInstance().checkPassword(mContext, apikey, password);
            }
            if (isPasswordOk) {
                PreyConfig.getPreyConfig(mContext).setTimePasswordOk();
            }
        } catch (PreyException  e1) {
            PreyLogger.e("login_tipo error1:" + e1.getMessage(), e1);
            error=e1.getMessage();
        } catch (Exception e) {
            PreyLogger.e("login_tipo error:" + e.getMessage(), e);
            error = e.getMessage();
        }
        PreyLogger.d("login_tipo isPasswordOk:" + isPasswordOk);
        PreyLogger.d("login_tipo error:" + error);
        if (error != null)
            return error;
        else if (!isPasswordOk) {
            wrongPasswordIntents++;
            if (wrongPasswordIntents == 3) {
                error = "{\"error\":[\"" + mContext.getString(R.string.password_intents_exceed) + "\"]}";
            } else {
                error = "{\"error\":[\"" + mContext.getString(R.string.password_wrong) + "\"]}";
            }
        } else {
            PreyLogger.d("login_tipo from:" + from);
            if ("setting".equals(from)) {
                //new Thread(new EventManagerRunner(mContext, new Event(Event.APPLICATION_OPENED))).start();
                return "{\"result\":true}";
            } else {
                Intent intent = new Intent(mContext, PanelWebActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                mActivity.finish();
            }
        }
        return error;
    }



    @JavascriptInterface
    public void openPanelWeb() {
        Intent intent = new Intent(mContext, PanelWebActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    @JavascriptInterface
    public String initPin4() {
        String initPin4 = PreyConfig.getPreyConfig(mContext).getPinNumber();
        PreyLogger.d("initPin4:" + initPin4);
        return initPin4;
    }

    @JavascriptInterface
    public String initVersion() {
        String initVersion = PreyConfig.getPreyConfig(mContext).getPreyVersion();
        PreyLogger.d("initVersion:" + initVersion);
        return initVersion;
    }


    @JavascriptInterface
    public boolean initVerify() {
        PreyLogger.d("initVerify users:");
        return false;
    }

    @JavascriptInterface
    public void setBackground(boolean background) {
        if (background) {
            RunBackgroundCheckBoxPreference.notifyReady(mContext);
        } else {
            RunBackgroundCheckBoxPreference.notifyCancel(mContext);
        }
        PreyConfig.getPreyConfig(mContext).setRunBackground(background);
    }

    @JavascriptInterface
    public void wipe() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.preferences_detach_summary)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PreyLogger.d("wipe:");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            new DetachDevice().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            new DetachDevice().execute();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        Dialog popup = builder.create();
        popup.show();
    }

    @JavascriptInterface
    public void savepin2(String pin) {
        PreyLogger.d("savepin2:" + pin);
        PreyConfig.getPreyConfig(mContext).setPinNumber(pin);
        if ("".equals(pin)) {
            setUninstall(false);
            setShieldOf(false);
        }
    }
    @JavascriptInterface
    public boolean getTwoStepEnabled() {
        return false;
    }

    @JavascriptInterface
    public boolean getTwoStepEnabled2() {
        PreyLogger.d("!PreyConfig.getPreyConfig(mContext).isTimeTwoStep():" + !PreyConfig.getPreyConfig(mContext).isTimeTwoStep());
        if (!PreyConfig.getPreyConfig(mContext).isTimeTwoStep()) {
            PreyLogger.d("!PreyConfig.getPreyConfig(mContext).isTimeTwoStep() dentro");
            boolean twoStepEnabled = PreyWebServices.getInstance().getTwoStepEnabled(mContext);
            PreyConfig.getPreyConfig(mContext).setTwoStep(twoStepEnabled);
            PreyConfig.getPreyConfig(mContext).setTimeTwoStep();
        }
        boolean twoStepEnabled = PreyConfig.getPreyConfig(mContext).getTwoStep();
        PreyLogger.d("twoStepEnabled isTimeTwoStep:" + twoStepEnabled);
        return twoStepEnabled;
    }

    @JavascriptInterface
    public int versionAndroid() {
        return android.os.Build.VERSION.SDK_INT;
    }

    @JavascriptInterface
    public boolean versionIsPieOrAbove() {
        return android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1;
    }

    @JavascriptInterface
    public void notificationShieldOf() {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(R.string.preferences_disable_power_alert_android9_title);
        alertDialog.setMessage(mContext.getString(R.string.preferences_disable_power_alert_android9_message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
        setShieldOf(false);
    }

    @JavascriptInterface
    public void setUninstall(boolean uninstall) {
        PreyLogger.d("setUninstall:" + uninstall);
        PreyConfig.getPreyConfig(mContext).setBlockAppUninstall(uninstall);
    }

    @JavascriptInterface
    public void setShieldOf(boolean shieldOf) {
        PreyLogger.d("setShieldOf:" + shieldOf);
        PreyConfig.getPreyConfig(mContext).setDisablePowerOptions(shieldOf);
        if (shieldOf) {
            mContext.startService(new Intent(mContext, PreyDisablePowerOptionsService.class));
        } else {
            mContext.stopService(new Intent(mContext, PreyDisablePowerOptionsService.class));
        }
    }

    @JavascriptInterface
    public void qr() {
        PreyLogger.d("qr");
        Intent intent = new Intent(mContext, BarcodeActivity.class);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    @JavascriptInterface
    public String initMail(){
        return PreyConfig.getPreyConfig(mContext).getEmail();
    }

    @JavascriptInterface
    public String lock(String key){
        PreyLogger.d("lock:"+key);
        String error2 = "";
        final Context ctx = mContext;
        String unlock = PreyConfig.getPreyConfig(ctx).getUnlockPass();
        PreyLogger.d("lock:"+key+" unlock:"+unlock);
        if (unlock != null && unlock.equals(key)) {
            PreyConfig.getPreyConfig(ctx).setLock(false);
            PreyConfig.getPreyConfig(ctx).deleteUnlockPass();
            new Thread() {
                public void run() {
                    String reason = "{\"origin\":\"user\"}";
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("start", "lock", "stopped", reason));
                }
            }.start();
            View viewLock=PreyConfig.getPreyConfig(ctx).viewLock;
            if(viewLock!=null){
                WindowManager wm = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
                wm.removeView(viewLock);
            }
            View viewSecure=PreyConfig.getPreyConfig(ctx).viewSecure;
            if(viewSecure!=null){
                WindowManager wm = (WindowManager) ctx.getSystemService(ctx.WINDOW_SERVICE);
                wm.removeView(viewSecure);
            }
            Intent intent = new Intent(ctx, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            try{Thread.sleep(2000);}catch (Exception e){}
            ctx.sendBroadcast(new Intent(CheckPasswordHtmlActivity.CLOSE_PREY));
        }else{
            error2 = "{\"error\":[\"" + mContext.getString(R.string.password_wrong) + "\"]}";
        }
        PreyLogger.d("error2:"+error2);
        return error2;
    }

    @JavascriptInterface
    public String changemail(String email){
        error = null;
        try {
            final Context ctx = mContext;
            PreyVerify verify=PreyWebServices.getInstance().verifyEmail(ctx,email);
            PreyLogger.d("verify:"+(verify==null?"":verify.getStatusCode()+" "+verify.getStatusDescription()));
            if(verify!=null){
                int statusCode=verify.getStatusCode();
                if(statusCode==HttpURLConnection.HTTP_OK||statusCode==HttpURLConnection.HTTP_CONFLICT){
                    PreyConfig.getPreyConfig(mContext).setEmail(email);
                    String okObj=mContext.getString(R.string.email_resend);
                    error="{\"ok\":[\"" + okObj + "\"]}";
                }else{
                    error = verify.getStatusDescription();
                    if(error!=null){
                        if(error.indexOf("error")<0) {
                            error = error.replace("\\\"", "'");
                            error = error.replace("\"", "");
                            error = error.replace("'", "\"");
                        }else{
                            JSONObject obj=new JSONObject(error);
                            String errorObj = obj.getString("error");
                            if (errorObj != null && errorObj.indexOf("[") < 0) {
                                error = "{\"email\":[\"" + errorObj + "\"]}";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            error = e.getMessage();
            PreyLogger.e("error:" + error, e);
        }
        return error;
    }

    @JavascriptInterface
    public String signup(String name, String email, String password1, String password2, String policy_rule_age, String policy_rule_privacy_terms,String offers) {
        PreyLogger.d("signup name: " + name + " email:" + email + " policy_rule_age:" + policy_rule_age + " policy_rule_privacy_terms:" + policy_rule_privacy_terms+" offers:"+offers);
        try {
            error = null;
            final Context ctx = mContext;
            PreyLogger.d("name:" + name);
            PreyLogger.d("email:" + email);
            PreyLogger.d("password1:" + password1);
            PreyLogger.d("password2:" + password2);
            PreyLogger.d("rule_age:" + policy_rule_age);
            PreyLogger.d("privacy_terms:" + policy_rule_privacy_terms);
            PreyLogger.d("offers:" + offers);
            PreyAccountData accountData = PreyWebServices.getInstance().registerNewAccount(ctx, name, email, password1, password2, policy_rule_age, policy_rule_privacy_terms, offers, PreyUtils.getDeviceType(mContext));
            PreyLogger.d("Response creating account: " + accountData.toString());
            PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
            PreyConfig.getPreyConfig(ctx).registerC2dm();
            PreyWebServices.getInstance().sendEvent(ctx, PreyConfig.ANDROID_SIGN_UP);
            PreyConfig.getPreyConfig(ctx).setEmail(email);
            PreyConfig.getPreyConfig(ctx).setRunBackground(true);
            PreyConfig.getPreyConfig(ctx).setInstallationStatus("Pending");
        } catch (Exception e) {
            error = e.getMessage();
            PreyLogger.e("error:" + error, e);
        }
        try {
            if (error == null) {
                error = "";
            }
        } catch (Exception e) {
        }
        PreyLogger.d("signup out:" + error);
        return error;
    }

    @JavascriptInterface
    public void forgot() {
        PreyLogger.d("forgot");
        String url = FileConfigReader.getInstance(mContext).getPreyForgot();
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mContext.startActivity(myIntent);
    }

    @JavascriptInterface
    public void givePermissions() {
        boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(mContext);
        boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(mContext);
        boolean canAccessCamera = PreyPermission.canAccessCamera(mContext);
        boolean canAccessPhone = PreyPermission.canAccessPhone(mContext);
        boolean canAccessStorage = PreyPermission.canAccessStorage(mContext);
        boolean canAccessBackgroundLocation = PreyPermission.canAccessBackgroundLocation(mContext);
        boolean showFineLocation = PreyPermission.showRequestFineLocation(mActivity);
        boolean showCoarseLocation = PreyPermission.showRequestCoarseLocation(mActivity);
        boolean showBackgroundLocation = PreyPermission.showRequestBackgroundLocation(mActivity);
        boolean showCamera = PreyPermission.showRequestCamera(mActivity);
        boolean showPhone = PreyPermission.showRequestPhone(mActivity);
        boolean showStorage = PreyPermission.showRequestStorage(mActivity);
        boolean canAccessibility = PreyPermission.isAccessibilityServiceEnabled(mContext);
        boolean showDeniedPermission=false;
        if(!canAccessStorage) {
            if (!showStorage)
                showDeniedPermission = true;
        }
        if(!canAccessFineLocation){
            if(!showFineLocation)
                showDeniedPermission=true;
        }
        if(!canAccessCoarseLocation){
            if(!showCoarseLocation)
                showDeniedPermission=true;
        }
        if(!canAccessCamera){
            if(!showCamera)
                showDeniedPermission=true;
        }
        if(!canAccessPhone){
            if(!showPhone)
                showDeniedPermission=true;
        }
        PreyLogger.d("canAccessFineLocation:" + canAccessFineLocation);
        PreyLogger.d("canAccessCoarseLocation:" + canAccessCoarseLocation);
        PreyLogger.d("canAccessBackgroundLocation:" + canAccessBackgroundLocation);
        PreyLogger.d("canAccessCamera:" + canAccessCamera);
        PreyLogger.d("canAccessPhone:" + canAccessPhone);
        PreyLogger.d("canAccessStorage:" + canAccessStorage);
        PreyLogger.d("showFineLocation:" + showFineLocation);
        PreyLogger.d("showCoarseLocation:" + showCoarseLocation);
        PreyLogger.d("showCamera:" + showCamera);
        PreyLogger.d("showPhoneState:" + showPhone);
        PreyLogger.d("showWriteStorage:" + showStorage);
        PreyLogger.d("canAccessibility:" + canAccessibility);
        if(!canAccessStorage&&!canAccessFineLocation&&!canAccessCoarseLocation&&!canAccessCamera&&!canAccessPhone&&
                !showStorage&&!showFineLocation&&!showCoarseLocation&&!showCamera&&!showPhone){
            showDeniedPermission=false;
        }
        if (showDeniedPermission) {
            mActivity.deniedPermission();
        } else {
                if (!canAccessFineLocation || !canAccessCoarseLocation || !canAccessCamera
                        || !canAccessPhone || !canAccessStorage ) {
                    mActivity.askForPermission();
                } else {
                    boolean canDrawOverlays = true;
                    if(PreyConfig.getPreyConfig(mContext).isOverOtherApps()){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            canDrawOverlays = Settings.canDrawOverlays(mContext);
                    }
                    if (!canDrawOverlays) {
                        mActivity.askForPermissionAndroid7();
                    } else {
                        boolean isAdminActive = FroyoSupport.getInstance(mContext).isAdminActive();
                        if (!isAdminActive) {
                            mActivity.askForAdminActive();
                        }else{
                            if(!canAccessibility){
                                mActivity.accessibility();
                            }else{
                                if(canAccessFineLocation||canAccessCoarseLocation) {
                                    if (Build.VERSION.SDK_INT >= PreyConfig.BUILD_VERSION_CODES_10 && !canAccessBackgroundLocation) {
                                        mActivity.deniedPermission();
                                    }
                                }
                            }
                        }
                    }
                }
        }


    }

    public class DetachDevice extends AsyncTask<Void, Void, Void> {
        private String error = null;
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(mContext.getText(R.string.preferences_detach_dettaching_message).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... unused) {
            error = Detach.detachDevice(mContext);
            PreyLogger.d("error:" + error);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
            try {
                if (error != null) {
                    Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
                } else {
                    Intent welcome = new Intent(mContext, CheckPasswordHtmlActivity.class);
                    welcome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(welcome);
                    mActivity.finish();
                }
            } catch (Exception e) {
            }
        }

    }

    @JavascriptInterface
    public boolean capsLockOn() {
        return PreyConfig.getPreyConfig(mContext).getCapsLockOn();
    }

    @JavascriptInterface
    public void touch() {
        PreyConfig.getPreyConfig(mContext).setCapsLockOn(false);
    }
}
