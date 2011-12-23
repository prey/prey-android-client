/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.prey.PreyController;
import com.prey.PreyLogger;
import com.prey.net.PreyRestHttpClient;

public class StopPreyPreference extends DialogPreference {

	public StopPreyPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		PreyLogger.d("Stopping Prey");
		if (which == DialogInterface.BUTTON_POSITIVE) {
			new Thread(new Runnable() {

				public void run() {
					try {
						
						PreyController.stopPrey(getContext());
					} catch (Exception e) {
						PreyLogger.e("Couldn't stop Prey", e);
					}
				}
			}).run();

			// new StopPreyTask().execute();

		}
	}

	private class StopPreyTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... data) {
			PreyController.stopPrey(getContext());
			return null;
		}

	}

}
