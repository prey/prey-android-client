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
import android.provider.ContactsContract;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class ContactsList extends JsonAction {

	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting ContactsList Data.");
		List<HttpDataService> listResult = super.get(ctx, list, parameters);
		return listResult;
	}

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		HttpDataService data = new HttpDataService("contacts_list");
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		Cursor cursor = null;
		try {
			final String[] projection = null;
			final String selection = null;
			final String[] selectionArgs = null;
			final String sortOrder = null;
			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
			cursor = ctx.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
			int i = 0;
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String phone = "";
					String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					try {
						phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					} catch (Exception e) {

					}
					if (!"".equals(phone)) {
						parametersMap.put(i + "][phone", phone);
						parametersMap.put(i + "][name", name);
						i++;
					}

				}
			}
		} catch (Exception ex) {
			PreyLogger.e("Error:"+ex.getMessage(), ex);
		} finally {
			cursor.close();
		}
		data.setList(true);
		data.addDataListAll(parametersMap);
		return data;
	}

}
