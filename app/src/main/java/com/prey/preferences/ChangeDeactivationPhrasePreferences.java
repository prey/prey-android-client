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

import com.prey.PreyLogger;
import com.prey.R;

public class ChangeDeactivationPhrasePreferences extends EditTextPreference {
	
	private Context ctx = null;
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
		super.onDialogClosed(positiveResult);
		if (positiveResult){
			PreyLogger.d("Deactivation phrase changed to:" + getText());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				new ChangeDeactivationPhraseTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,getText());
			else
				new ChangeDeactivationPhraseTask().execute(getText());
		}
	}
	
	private class ChangeDeactivationPhraseTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog = null;
		 
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
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
		}
	}

}