package com.prey.json.actions;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.SimpleCameraActivity;
import com.prey.activities.SimpleVideoActivity;

import com.prey.json.JsonAction;

public class Video extends JsonAction {

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		return null;
	}

	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		SimpleVideoActivity.activity = null;
		try {
			Intent intent = new Intent(ctx, SimpleVideoActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(intent);
			
			int i = 0;
			while (SimpleVideoActivity.activity == null && i < 10) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				PreyLogger.i("esperando antes take [" + i + "]");
				i++;
			}

			SimpleVideoActivity.activity.takeVideo();
			try {
					Thread.sleep(30000);
			} catch (InterruptedException e) {
			}
			SimpleVideoActivity.activity.stopRecording();
			SimpleVideoActivity.activity.sendVideo(ctx);

		} finally {
			SimpleCameraActivity.activity.finish();
			SimpleCameraActivity.activity = null;
		}
	}

}
