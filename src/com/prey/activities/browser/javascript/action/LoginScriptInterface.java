package com.prey.activities.browser.javascript.action;


import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.R;



 
import com.prey.activities.browser.manager.ManagerBrowser;
import com.prey.analytics.PreyGoogleAnalytics;
import com.prey.exceptions.NoMoreDevicesAllowedException;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

import android.app.ProgressDialog;
import android.content.Context;



import android.os.AsyncTask;

import android.widget.Toast;

public class LoginScriptInterface {

	private Context ctx = null;

 

	private String error = null;
	private boolean noMoreDeviceError = false;

	private int wrongPasswordIntents;
	
	ProgressDialog progressDialog = null;
	
	public LoginScriptInterface(Context ctx) {
		this.ctx = ctx;
	}

	public void execute(String email, String password, String deviceType, int wrongPasswordIntents) {
		this.wrongPasswordIntents=wrongPasswordIntents;
		PreyLogger.i("login(" + email + "," + password + ")");
		if (!PreyConfig.getPreyConfig(ctx).isThisDeviceAlreadyRegisteredWithPrey()) {
			new AddDeviceToAccount().execute(email, password, deviceType);
		} else {
			new CheckPassword().execute(password);
		 
		}

	}

	 

	 
	
	
	
	private class AddDeviceToAccount extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ctx);
			progressDialog.setMessage(ctx.getText(R.string.set_old_user_loading).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... data) {
			try {
				noMoreDeviceError = false;
				error = null;
				PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceToAccount(ctx, data[0], data[1],
						data[2]);
				PreyConfig.getPreyConfig(ctx).saveAccount(accountData);

			} catch (PreyException e) {
				error = e.getMessage();
				try {
					NoMoreDevicesAllowedException noMoreDevices = (NoMoreDevicesAllowedException) e;
					PreyLogger.d("Message:"+noMoreDevices.getMessage());
					noMoreDeviceError = true;
				} catch (ClassCastException e1) {
					noMoreDeviceError = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			if (noMoreDeviceError){
				Toast.makeText(ctx,R.string.set_old_user_no_more_devices_text, Toast.LENGTH_LONG).show();
			}else {
				if (error == null) {
					PreyGoogleAnalytics.getInstance().trackAsynchronously(ctx,"Device/Added");
 
					PreyConfig preyConfig = PreyConfig.getPreyConfig(ctx);
					preyConfig.setActiveTour(false);
					
					ManagerBrowser manager = new ManagerBrowser();
					manager.postLogin(ctx);
					preyConfig.registerC2dm();
				} else {
					Toast.makeText(ctx,error, Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	
	protected class CheckPassword extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog = null;
		boolean isPasswordOk = false;
		boolean keepAsking = true;
		String error = null;

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(ctx);
			progressDialog.setMessage(ctx.getText(R.string.password_checking_dialog).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... password) {
			try {
				String email = PreyConfig.getPreyConfig(ctx).getEmail();
				isPasswordOk = PreyWebServices.getInstance().checkPassword(ctx, email, password[0]);
			} catch (PreyException e) {
				error = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			if (progressDialog.isShowing())
				progressDialog.dismiss();
			if (error != null)
				Toast.makeText(ctx, error, Toast.LENGTH_LONG).show();
			else if (!isPasswordOk) {
				boolean isAccountVerified = PreyConfig.getPreyConfig(ctx).isAccountVerified();
				if (!isAccountVerified)
					Toast.makeText(ctx, R.string.verify_your_account_first, Toast.LENGTH_LONG).show();
				else {
					wrongPasswordIntents++;
					if (wrongPasswordIntents == 3) {
						Toast.makeText(ctx, R.string.password_intents_exceed, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(ctx, R.string.password_wrong, Toast.LENGTH_LONG).show();
					}		
				}
			} else {
				wrongPasswordIntents=0;
				ManagerBrowser manager = new ManagerBrowser();
				manager.postLogin(ctx);
			}
		}

	}
}
