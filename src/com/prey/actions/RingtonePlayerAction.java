package com.prey.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.observer.ActionJob;

public class RingtonePlayerAction extends PreyAction {

	public static final String DATA_ID = "ringtone";
	public final String ID = "ringtone";

	@Override
	public String textToNotifyUserOnEachReport(Context ctx) {
		return "";
	}

	@Override
	public void execute(ActionJob actionJob, Context ctx) {
		PreyLogger.d("Ejecuting RingtonePlayerAction Action");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
		String ringtoneToPlay = settings.getString(PreyConfig.PREFS_RINGTONE, null);
		Uri soundUri = TextUtils.isEmpty(ringtoneToPlay) ? null : Uri.parse(ringtoneToPlay);
		Uri toPlay = soundUri == null ? RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_RINGTONE) : soundUri;
		RingtoneManager.getRingtone(ctx, toPlay).play();

		PreyLogger.d("Ejecuting RingtonePlayerAction Action. DONE!");
	}

	@Override
	public boolean isSyncAction() {
		return false;
	}

	@Override
	public boolean shouldNotify() {
		return false;
	}

}
