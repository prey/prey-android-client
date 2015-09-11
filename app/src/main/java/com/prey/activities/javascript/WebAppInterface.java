package com.prey.activities.javascript;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.CheckPasswordActivity;
import com.prey.activities.PermissionInformationActivity;
import com.prey.activities.PreyConfigurationActivity;
import com.prey.activities.WebViewInitActivity;
import com.prey.activities.WebViewReadyActivity;
import com.prey.events.Event;
import com.prey.events.manager.EventManagerRunner;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

public class WebAppInterface {

    private Context context;
    private String error = null;
    private WebViewInitActivity activityInit=null;
    private WebViewReadyActivity activityReady=null;


    /**
     * Instantiate the interface and set the context
     *
     * @param context
     */
    public WebAppInterface(Context context) {
        this.context = context;
    }


    public WebViewInitActivity getActivityInit() {
        return activityInit;
    }

    public void setActivityInit(WebViewInitActivity activityInit) {
        this.activityInit = activityInit;
    }
    public void setActivityReady(WebViewReadyActivity activityReady) {
        this.activityReady = activityReady;
    }

    /**
     * Show a dialog from the web page.
     *
     * @param message
     *            message of the dialog
     */
    @JavascriptInterface
    public void showDialog(String message) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(message).setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }


    @JavascriptInterface
    public void login(String email,String password) {
        PreyLogger.i("login(" + email + "," + password + ")");
        if (email == null || email.equals("") || password == null || password.equals("")) {
            Toast.makeText(context, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
        } else {
            if (email.length() < 6 || email.length() > 100) {
                Toast.makeText(context, context.getString(R.string.error_mail_out_of_range, 6, 100), Toast.LENGTH_LONG).show();
            } else {
                if (password.length() < 6 || password.length() > 32) {
                    Toast.makeText(context, context.getString(R.string.error_password_out_of_range, 6, 32), Toast.LENGTH_LONG).show();
                } else {
                    activityInit.addDeviceToAccount(email, password);
                }
            }
        }
    }

    @JavascriptInterface
    public void register(String name,String email,String password){
        PreyLogger.i("register(" + name + "," + email + "," + password + ")");

        if (email == null || email.equals("") || password == null || password.equals("")) {
            Toast.makeText(context, R.string.error_all_fields_are_required, Toast.LENGTH_LONG).show();
        } else {
            if (email.length() < 6 || email.length() > 100) {
                Toast.makeText(context, context.getString(R.string.error_mail_out_of_range, 6, 100), Toast.LENGTH_LONG).show();
            } else {
                if (password.length() < 6 || password.length() > 32) {
                    Toast.makeText(context, context.getString(R.string.error_password_out_of_range, 6, 32), Toast.LENGTH_LONG).show();
                } else {
                    activityInit.createAccount(name, email, password);
                }
            }
        }
    }




    @JavascriptInterface
    public void  closeTour(){
        PreyLogger.i("closeTour");
        PreyConfig.getPreyConfig(context).setProtectTour(true);
    }

    @JavascriptInterface
    public void  remoteControl(){

        PreyLogger.i("remoteControl");
        String url = PreyConfig.getPreyConfig(context).getPreyPanelUrl();
        PreyLogger.i("url:"+url);
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        context.startActivity(browserIntent);

    }

    @JavascriptInterface
    public void  uninstall(){

        PreyLogger.i("remoteControl");
        String url = PreyConfig.getPreyConfig(context).getPreyUninstallUrl();
        PreyLogger.i("url:"+url);
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        context.startActivity(browserIntent);

    }

    @JavascriptInterface
    public void settings() {

        PreyLogger.i("settings");
        if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
            Intent intent = new Intent(context, CheckPasswordActivity.class);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, PreyConfigurationActivity.class);
            context.startActivity(intent);
        }
    }

    @JavascriptInterface
    public void signIn(String pass) {
        PreyLogger.i("signIn(" + pass + ")");



        if (pass.equals(""))
            Toast.makeText(context, R.string.preferences_password_length_error, Toast.LENGTH_LONG).show();
        else {
            if (pass.length() < 6 || pass.length() > 32) {
                Toast.makeText(context, context.getString(R.string.error_password_out_of_range, 6, 32), Toast.LENGTH_LONG).show();
            } else {
                activityReady.checkPassword(pass);
            }
        }




    }
    @JavascriptInterface
    public void forgotPassword() {
        PreyLogger.i("forgotPassword");

        String url = PreyConfig.getPreyConfig(context).getPreyUrl();
        PreyLogger.i("url:"+url);
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        context.startActivity(browserIntent);
    }





    @JavascriptInterface
    public void activateProtection(){

    }

}