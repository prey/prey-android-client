package com.prey.json.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.contact.dto.ContactDto;
import com.prey.actions.contact.dto.EmailDto;
import com.prey.actions.contact.dto.PhoneDto;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.parser.JSONParser;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyRestHttpClient;
import com.prey.net.PreyWebServices;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class ContactsRestore extends JsonAction {

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
		HttpDataService data = new HttpDataService("contacts_restore");
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		String sb = null;
		String json = null;
		PreyHttpResponse response = PreyWebServices.getInstance().getContact(ctx);
		sb = response.getResponseAsString();
		
		//sb="  {\"0\":{\"nickname\":{\"name\":\"Jano\",\"label\":\"Jano\"},\"phones\":{\"0\":{\"type\":\"mobile\",\"number\":\"+56998201358\"}},\"display_name\":\"Jano\"}}";
		//sb=sb+"\"1\":{\"nickname\":{\"name\":\"Renato Millas\",\"label\":\"Renato Millas\"},\"display_name\":\"Renato Millas\",\"emails\":{\"0\":{\"address\":\"renato.millas@signos-ti.cl\",\"type\":\"home\"}},\"phones\":{\"1\":{\"number\":\"+56977592028\",\"type\":\"work\"},\"0\":{\"number\":\"+56992571129\",\"type\":\"mobile\"}}}," ;
		//sb=sb+"\"2\":{\"nickname\":{\"name\":\"Matias Carrasco\",\"label\":\"Matias Carrasco\"},\"phones\":{\"1\":{\"type\":\"work\",\"number\":\"+56964127768\"},\"0\":{\"number\":\"+56977499606\",\"type\":\"mobile\"}},\"display_name\":\"Matias Carrasco\"},";
		//sb=sb+"\"3\":{\"nickname\":{\"name\":\"Italo Hevia\",\"label\":\"Italo Hevia\",},\"display_name\":\"Italo Hevia\",\"phones\":{\"0\":{\"type\":\"mobile\",\"number\":\"+56998185858\"}}}";
		if (sb != null)
			json = sb.trim();

		PreyLogger.i("sb length:" + sb.length());
		PreyLogger.i("sb:" + sb);
		JSONObject jsonArray = null;
		try {
			jsonArray = new JSONObject(json);

		} catch (Exception e) {
			PreyLogger.i("Error, causa:" + e.getMessage());
		}

		try {

			for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {

				ContactDto contactDto = new ContactDto();
				JSONObject contact = jsonArray.getJSONObject("" + i);
				String displayName = contact.getString("display_name");
				JSONObject nickname = contact.getJSONObject("nickname");
				String nicknameName = nickname.getString("name");
				String nicknameLabel = nickname.getString("label");

				contactDto.setDisplayName(displayName);
				PreyLogger.i("[" + i + "]displayName:" + displayName + " nicknameName:" + nicknameName + " nicknameLabel:" + nicknameLabel);
				String phones = null;
				try {
					phones = contact.getJSONObject("phones").toString();
				} catch (Exception e) {
				}
				if (phones != null) {
					JSONObject arrayPhone = new JSONObject(phones);
					for (int j = 0; arrayPhone != null && j < arrayPhone.length(); j++) {
						JSONObject phone = arrayPhone.getJSONObject("" + j);
						String phoneNumber = phone.getString("number");
						String phoneType = phone.getString("type");
						PhoneDto phoneDto = new PhoneDto();
						phoneDto.setNumber(phoneNumber);
						phoneDto.setType(typePhone(phoneType));
						contactDto.addPhone(phoneDto);
						PreyLogger.i("[" + i + "][" + j + "]phoneNumber:" + phoneNumber + " phoneType:" + phoneType);
					}
				}

				String emails = null;
				try {
					emails = contact.getJSONObject("emails").toString();
				} catch (Exception e) {
				}
				if (emails != null) {
					JSONObject arrayMail = new JSONObject(emails);
					for (int j = 0; arrayMail != null && j < arrayMail.length(); j++) {
						JSONObject mail = arrayMail.getJSONObject("" + j);
						String mailAddress = mail.getString("address");
						String mailType = mail.getString("type");
						EmailDto emailDto = new EmailDto();
						emailDto.setEmail(mailAddress);
						emailDto.setType(typeMail(mailType));
						contactDto.addEmail(emailDto);

						PreyLogger.i("[" + i + "][" + j + "]mailAddress:" + mailAddress + " mailType:" + mailType);
					}
				}
				addContactDto(ctx, contactDto);
			}
		} catch (Exception e) {
			PreyLogger.i("Error, causa:" + e.getMessage());
		}

		data.setList(true);
		data.addDataListAll(parametersMap);
		return data;
	}

	public void addContactDto(Context ctx, ContactDto contact) {
		try {
			 
			
			
			 
			
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	        int rawContactInsertIndex = ops.size();
	        
	        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
	                .withValue(RawContacts.ACCOUNT_TYPE, null)
	                .withValue(RawContacts.ACCOUNT_NAME, null)
	                .build());
	        
	        
	        //INSERT NAME
	        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
	                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getDisplayName()) // Name of the person
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contact.getNickName()) // Name of the person
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.getNickName()) // Name of the person
	                .build());
	          
	        
	        List<PhoneDto> phones= contact.getPhones();
	        for(int i=0;phones!=null&&i<phones.size();i++){
	        	PhoneDto phoneDto=phones.get(i);
	        	//INSERT MOBILE
	        	ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
	                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
	                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneDto.getNumber()) // Number of the person
	                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
	                .build()); //
	        	
	        }
	        
	        List<EmailDto> emails=contact.getEmails();
	        for(int i=0;emails!=null&&i<emails.size();i++){
	        	EmailDto emailDto=emails.get(i);
	        	ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI) 
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)

	            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
	            .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailDto.getEmail())
	            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
	            .build());
	        }
	        
	        
	        try {
        	    ctx. getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        	 } catch (Exception e) {
        	     PreyLogger.i("Error, causa:"+e.getMessage());
        	 }
	        
	        
	        
		} catch (Exception e) {
			PreyLogger.i("Error, causa:"+e.getMessage());
		}
	}

	public int typeMail(String type) {

		if ("home".equals(type)) {
			return 1;
		}
		if ("mobile".equals(type)) {
			return 4;
		}
		if ("home".equals(type)) {
			return 3;
		}

		return 4;// other change

	}

	public int typePhone(String type) {
		if ("home".equals(type)) {
			return 1;
		}
		if ("mobile".equals(type)) {
			return 2;
		}
		if ("home".equals(type)) {
			return 7;
		}
		return 7;// other change
	}

}
