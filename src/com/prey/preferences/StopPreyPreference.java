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

public class StopPreyPreference extends DialogPreference {

	public StopPreyPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			new StopPreyTask().execute();
			
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
