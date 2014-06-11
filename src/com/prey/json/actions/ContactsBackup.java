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

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract; 

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService; 
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction; 
import com.prey.net.PreyWebServices;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class ContactsBackup extends JsonAction {

	public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		PreyLogger.d("Ejecuting ContactsList Data.");
		List<HttpDataService> listResult = super.get(ctx, list, parameters);
		return listResult;
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		return start(ctx, lista, parameters);
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public HttpDataService start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		HttpDataService data = new HttpDataService("contacts_backup");
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		try {
		 
			parametersMap=contacts2(ctx);
			PreyWebServices.getInstance().sendContact(ctx, parametersMap);
		} catch (Exception ex) {
			PreyLogger.e("Error:" + ex.getMessage(), ex);

		}
		data.setList(true);
		data.addDataListAll(parametersMap);
		return data;
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public HashMap<String, String> contacts2(Context ctx) {
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		String phoneNumber = null;
		int phoneType;

		String email = null;
		int emailType;

		Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

		String _ID = ContactsContract.Contacts._ID;

		String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

		 

		String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

		Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

		String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

		String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
		String NUMBER_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;

		Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;

		String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;

		String DATA = ContactsContract.CommonDataKinds.Email.DATA;
		String DATATYPE = ContactsContract.CommonDataKinds.Email.TYPE;

		Cursor cursor = ctx.getContentResolver().query(CONTENT_URI, null, null, null, null);

		// Loop for every contact in the phone
		
		if (cursor.getCount() > 0) {

			 

			int i = 0;
			while (cursor.moveToNext()) {

				String contact_id = cursor.getString(cursor.getColumnIndex(_ID));

				String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

				 

				int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

				if (hasPhoneNumber > 0) {

					PreyLogger.i("____________");
					parametersMap.put("contacts_backup[" + i + "][display_name]", name);
					parametersMap.put("contacts_backup[" + i + "][nickname][name]", name);
					parametersMap.put("contacts_backup[" + i + "][nickname][label]", name);

					PreyLogger.i("contacts_backup[" + i + "][display_name]" + name);
					PreyLogger.i("contacts_backup[" + i + "][nickname][name]" + name);
					PreyLogger.i("contacts_backup[" + i + "][nickname][label]" + name);

					// Query and loop for every phone number of the contact

					Cursor phoneCursor = ctx.getContentResolver().query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

					int j = 0;
					while (phoneCursor.moveToNext()) {

						phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
						phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(NUMBER_TYPE));

						parametersMap.put("contacts_backup[" + i + "][phones][" + j + "][number]", phoneNumber);
						parametersMap.put("contacts_backup[" + i + "][phones][" + j + "][type]", typePhone(phoneType));

						PreyLogger.i("contacts_backup[" + i + "][phones][" + j + "][number]" + phoneNumber);
						PreyLogger.i("contacts_backup[" + i + "][phones][" + j + "][type]" + typePhone(phoneType));

						j++;

					}// while phone

					phoneCursor.close();

					// Query and loop for every email of the contact

					Cursor emailCursor = ctx.getContentResolver().query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[] { contact_id }, null);

					j = 0;
					while (emailCursor.moveToNext()) {

						email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
						emailType = emailCursor.getInt(emailCursor.getColumnIndex(DATATYPE));
						parametersMap.put("contacts_backup[" + i + "][emails][" + j + "][address]", email);
						parametersMap.put("contacts_backup[" + i + "][emails][" + j + "][type]", typeMail(emailType));

						PreyLogger.i("contacts_backup[" + i + "][emails][" + j + "][address]" + email);
						PreyLogger.i("contacts_backup[" + i + "][emails][" + j + "][type]" + typeMail(emailType));
						j++;
					}// while email

					emailCursor.close();
					i=i+1;
				}// if hasPhoneNumber
			
			}// while cursor
				// 
		}// if cursor
		return parametersMap;
		
	}

	public String typeMail(int type) {
		String out = "";
		switch (type) {
		case 1:
			out = "home";
			break;
		case 4:
			out = "mobile";
			break;
		case 3:
			out = "other";
			break;
		default:
			out = "work";
			break;
		}
		return out;
	}

	public String typePhone(int type) {
		String out = "";
		switch (type) {
		case 1:
			out = "home";
			break;
		case 2:
			out = "mobile";
			break;
		case 7:
			out = "other";
			break;
		default:
			out = "work";
			break;
		}
		return out;
	}

}
