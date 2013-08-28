package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class Ring extends JsonAction {

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		return null;
	}

	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
	
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(ctx, notification);
		r.play();
	}
}
