package com.prey.json.actions;

import java.util.List;
import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;

import com.prey.actions.location.LocationThread;

import com.prey.actions.observer.ActionResult;

public class Location {

	public void sms(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting sms Location Data.");
		String phoneNumber = null;
		if (parameters != null) {
			try {
				phoneNumber = parameters.getString("parameter");
			} catch (Exception e) {
				try {
					phoneNumber = parameters.getString("phoneNumber");
				} catch (Exception e1) {
					PreyLogger.e("Error, causa:" + e1.getMessage(), e1);
				}
			}

			try {
				if (phoneNumber != null && !"".equals(phoneNumber)) {
					new LocationThread(ctx, phoneNumber).start();
				}
			} catch (Exception e) {
				PreyLogger.e("Error, causa:" + e.getMessage(), e);
			}
		}
	}

}
