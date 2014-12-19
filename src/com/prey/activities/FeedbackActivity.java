package com.prey.activities;

import java.util.Calendar;
import java.util.Date;

import com.prey.PreyConfig;
import com.prey.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

public class FeedbackActivity extends PreyActivity {

	private static final int SHOW_POPUP = 0;

	public static int FLAG_FEEDBACK_INIT = 0;
	public static int FLAG_FEEDBACK_C2DM = 1;
	public static int FLAG_FEEDBACK_SEND = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		showDialog(SHOW_POPUP);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog popup = null;
		switch (id) {

		case SHOW_POPUP:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setIcon(R.drawable.logo);
			alert.setTitle(R.string.feedback_principal_title);
			alert.setMessage(R.string.feedback_principal_message);

			alert.setPositiveButton(R.string.feedback_principal_button1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=com.prey"));
					startActivity(intent);
					PreyConfig.getPreyConfig(getApplicationContext()).setFlagFeedback(FLAG_FEEDBACK_SEND);
					finish();
				}
			});

			alert.setNeutralButton(R.string.feedback_principal_button2, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent popup = new Intent(getApplicationContext(), FormFeedbackActivity.class);
					popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(popup);
					PreyConfig.getPreyConfig(getApplicationContext()).setFlagFeedback(FLAG_FEEDBACK_SEND);
					finish();
				}
			});

			alert.setNegativeButton(R.string.feedback_principal_button3, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					PreyConfig.getPreyConfig(getApplicationContext()).setFlagFeedback(FLAG_FEEDBACK_SEND);
					finish();
				}
			});
			popup = alert.create();
		}
		return popup;
	}

	public static boolean showFeedback(long installationDate, int flagFeedback) {
		if (flagFeedback == FLAG_FEEDBACK_C2DM) {
			return true;
		} else {
			if (flagFeedback == FLAG_FEEDBACK_INIT) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(installationDate);
				cal.add(Calendar.WEEK_OF_YEAR, 2);
				long twoWeekOfYear = cal.getTimeInMillis();
				long now = new Date().getTime();
				if (now > twoWeekOfYear) {
					return true;
				}
			}
		}
		return false;
	}
}