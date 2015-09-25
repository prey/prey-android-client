/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;

public class RevokedPasswordPreferences extends EditTextPreference {


    Context ctx = null;
    private String error = null;

    public RevokedPasswordPreferences(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.ctx = context;
    }

    public RevokedPasswordPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    public RevokedPasswordPreferences(Context context) {
        super(context);
        this.ctx = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // TODO Auto-generated method stub
        super.onDialogClosed(positiveResult);
        PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
        if (positiveResult) {
            PreyLogger.d("Activation phrase changed to:" + getText());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new RevokedPasswordPhraseTask(ctx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getText());
            else
                new RevokedPasswordPhraseTask(ctx).execute(getText());
        } else {
            preyConfig.setRevokedPassword(false, "");
        }
    }


    private class RevokedPasswordPhraseTask extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;
        private Context context = null;

        public RevokedPasswordPhraseTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getContext().getText(R.string.preferences_admin_device_setting_uninstallation_password).toString());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                PreyConfig preyConfig = PreyConfig.getPreyConfig(context);
                PreyLogger.d("password [" + getText() + "]");
                preyConfig.setRevokedPassword(true, getText());

                //PreyWebServices.getInstance().updateActivationPhrase(getContext(), getText());
            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressDialog.dismiss();
        }

    }

}
