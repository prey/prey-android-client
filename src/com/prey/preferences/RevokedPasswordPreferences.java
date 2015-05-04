/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

 
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.services.RevokedPasswordPhraseService;

public class RevokedPasswordPreferences extends EditTextPreference {
	
	public static final String REVOKEDPWD_FILTER = "RevokedPasswordPreferences_receiver";

	Context ctx = null;
	private String error = null;
	private RevokedPasswordPhraseReceiver receiver;
	
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
		if (positiveResult){
			PreyLogger.d("Activation phrase changed to:" + getText());
			receiver = new RevokedPasswordPhraseReceiver();
			ctx.registerReceiver(receiver, new IntentFilter(REVOKEDPWD_FILTER));
			receiver.showProgressDialog();
			Intent revokedPwdPhrase = new Intent(ctx, RevokedPasswordPhraseService.class);
			revokedPwdPhrase.putExtra("param", getText());
			ctx.startService(revokedPwdPhrase);
		}
		else{
			preyConfig.setRevokedPassword(false, "");
		}
	}

	@Override
	public void onActivityDestroy() {
		super.onActivityDestroy();
		if (receiver != null) {
			ctx.unregisterReceiver(receiver);
		}
	}

	private class RevokedPasswordPhraseReceiver extends BroadcastReceiver {

		ProgressDialog progressDialog = null;
		 
		public void showProgressDialog() {
			
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setMessage(getContext().getText(R.string.preferences_admin_device_setting_uninstallation_password).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		public void onReceive(Context receiverContext, Intent receiverIntent) {
			error = receiverIntent.getStringExtra("error");
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}

	}

}
