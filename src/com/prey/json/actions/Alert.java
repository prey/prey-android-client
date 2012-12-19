package com.prey.json.actions;

 

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.actions.observer.ActionResult;
import com.prey.activities.PopUpAlertActivity;
import com.prey.net.PreyWebServices;

public class Alert {

	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		try {
			String title = "title";//parameters.getString("title");
			String description = parameters.getString("message");


			Bundle bundle = new Bundle();
			bundle.putString("title_message", title);
			bundle.putString("description_message", description);

			Intent popup = new Intent(ctx, PopUpAlertActivity.class);
			popup.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			popup.putExtras(bundle);
			ctx.startActivity(popup);

			PreyWebServices.getInstance().sendEventsPreyHttpReport(ctx, "alert_started", "true");
			try {
				int i = 0;
				while (!PreyStatus.getInstance().isPreyPopUpOnclick() && i < 10) {
					Thread.sleep(1000);
					i++;
				}
			} catch (InterruptedException e) {
			}

		} catch (JSONException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}
	}

}
