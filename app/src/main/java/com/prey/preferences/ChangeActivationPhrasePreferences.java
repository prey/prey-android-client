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
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.prey.PreyLogger;
import com.prey.net.PreyWebServices;
import com.prey.R;

public class ChangeActivationPhrasePreferences extends EditTextPreference {


    Context ctx = null;
    private String error = null;

    public ChangeActivationPhrasePreferences(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.ctx = context;
    }

    public ChangeActivationPhrasePreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    public ChangeActivationPhrasePreferences(Context context) {
        super(context);
        this.ctx = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // TODO Auto-generated method stub
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            PreyLogger.d("Activation phrase changed to:" + getText());
            new ChangeActivationPhraseTask().execute(getText());

        }
    }


    private class ChangeActivationPhraseTask extends AsyncTask<String, Void, Void> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage(getContext().getText(R.string.updating_info_message).toString());
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(String... data) {
            /*try {
				PreyWebServices.getInstance().updateActivationPhrase(getContext(), getText());
			} catch (Exception e) {
				error = e.getMessage();
			}*/
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {

            }
        }

    }

}
