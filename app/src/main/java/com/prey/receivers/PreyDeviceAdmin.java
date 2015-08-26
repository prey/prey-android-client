package com.prey.receivers;

/**
 * Created by oso on 19-08-15.
 */

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

import com.prey.R;
public class PreyDeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        PreyLogger.d("Device Admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {

        //	FroyoSupport.getInstance(context).lockNow();

        return context.getText(R.string.preferences_admin_enabled_dialog_message).toString();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        PreyLogger.d("Device Admin disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        PreyLogger.d("Password was changed successfully");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);

        /*
        if (preyConfig.isLockSet()){
            PreyLogger.d("Password was entered successfully");
            preyConfig.setLock(false);
            FroyoSupport.getInstance(context).changePasswordAndLock("", false);
            final Context contexfinal=context;
            new Thread(){
                public void run() {
                    PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(contexfinal, UtilJson.makeMapParam("stop","lock","stopped"));
                }
            }.start();
        }*/
    }

}
