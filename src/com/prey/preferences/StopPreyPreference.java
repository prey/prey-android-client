package com.prey.preferences;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.prey.PreyConfig;
import com.prey.PreyController;
import com.prey.R;
import com.prey.net.PreyWebServices;

public class StopPreyPreference extends DialogPreference {

	private Context ctx;
	public StopPreyPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			new StopPreyTask().execute();
			
		}
	}
	
	private class StopPreyTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ctx);
			progressDialog.setMessage(ctx.getText(R.string.preferences_stop_dialog_title).toString());
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... data) {
			//PreyWebServices.getInstance().setMissing(ctx, false);
			//PreyConfig.getPreyConfig(ctx).setMissing(false);
			PreyController.stopPrey(getContext());
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
		}

	}

}
