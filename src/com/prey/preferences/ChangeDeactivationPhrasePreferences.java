/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
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
import com.prey.R;
import com.prey.net.PreyWebServices;

public class ChangeDeactivationPhrasePreferences extends EditTextPreference {
	
	
	Context ctx = null;
	private String error = null;
	
	public ChangeDeactivationPhrasePreferences(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public ChangeDeactivationPhrasePreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public ChangeDeactivationPhrasePreferences(Context context) {
		super(context);
		this.ctx = context;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		super.onDialogClosed(positiveResult);
		if (positiveResult){
			PreyLogger.d("Deactivation phrase changed to:" + getText());
			new ChangeDeactivationPhraseTask().execute(getText());
		}
	}
	
	private class ChangeDeactivationPhraseTask extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog = null;
		 
		@Override
		protected void onPreExecute() {
			
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setMessage(getContext().getText(R.string.updating_info_message).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... data) {
			try {
				PreyWebServices.getInstance().updateDeactivationPhrase(ctx, getText());
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
