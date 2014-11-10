/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.prey.R;
public class PopUpAlertActivity extends PreyActivity {

	private static final int SHOW_POPUP = 0;
	private String message = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.message = bundle.getString("alert_message");
		}

		showDialog(SHOW_POPUP);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog popup = null;
		switch (id) {

		case SHOW_POPUP:
			popup = new AlertDialog.Builder(PopUpAlertActivity.this).setTitle(R.string.popup_alert_title).setMessage(this.message)
					.setCancelable(true).create();

			popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
		}
		return popup;
	}

}
