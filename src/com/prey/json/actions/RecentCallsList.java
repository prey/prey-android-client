/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class RecentCallsList extends JsonAction {
	public List<HttpDataService> report(Context ctx, List<ActionResult> list, JSONObject parameters) {
		List<HttpDataService> listResult = super.report(ctx, list, parameters);
		return listResult;
	}

	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting RecentCallsList Data.");
		List<HttpDataService> listResult = super.get(ctx, list, parameters);
		return listResult;
	}

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		HttpDataService data = new HttpDataService("recent_calls_list");
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		final String[] projection = null;
		final String selection = null;
		final String[] selectionArgs = null;
		final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
		Cursor cursor = null;

		try {
			cursor = ctx.getContentResolver().query(Uri.parse("content://call_log/calls"), projection, selection, selectionArgs, sortOrder);
			int i = 0;
			while (cursor.moveToNext()) {
				// String callLogID =
				// cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls._ID));
				String callNumber = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
				String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
				String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));
				// String isCallNew =
				// cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW));
				String duration = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION));
				parametersMap.put(i + "][number", callNumber);
				parametersMap.put(i + "][date", callDate);
				parametersMap.put(i + "][type", callType);
				parametersMap.put(i + "][duration", duration);
				i++;
			}
			data.setList(true);
			data.addDataListAll(parametersMap);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}

		return data;
	}
}
