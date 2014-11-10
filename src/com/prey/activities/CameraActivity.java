package com.prey.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.PreyStatus;

public class CameraActivity  extends PreyActivity {

	private static final int SHOW_POPUP = 0;
	private String message = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.message = bundle.getString("webcam_message");
		}

		showDialog(SHOW_POPUP);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog popup = null;
		switch (id) {

		case SHOW_POPUP:
			popup = new AlertDialog.Builder(CameraActivity.this).setIcon(R.drawable.logo).setTitle(R.string.popup_alert_title).setMessage(this.message)
					.setCancelable(true).create();

			popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					PreyLogger.d("CameraActivity onDismiss");
					PreyStatus.getInstance().setTakenPicture(true);
					
					finish();
				}
			});
		}
		return popup;
	}

}
