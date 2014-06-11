package com.prey.actions.contact;

import org.json.JSONArray;

import com.prey.PreyLogger; 

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri; 
import android.os.Build;
import android.provider.ContactsContract;
 

public class ContactUtil {

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static JSONArray history(Context ctx){
		JSONArray array=new JSONArray();
		Cursor c = null;
		try{
			PreyContact objContact= new PreyContact();
			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	     	c = ctx.getContentResolver().query(uri, null, null, null, null);
	        int totalCall = c.getCount();
            if (c.moveToFirst()) {
            	for (int i = 0; i < totalCall; i++) {
            		objContact = new PreyContact();
            		
            		
            		
            		Phone phone=new Phone();
            		phone.setNumber(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            		phone.setType(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
            		Photo photo=new Photo();
            		photo.setDisplayName(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Photo.DISPLAY_NAME)));
            		photo.setPhoto(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)));
            		NickName nickname= new NickName();
            		nickname.setLabel(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.LABEL)));
            		nickname.setName(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)));
            		nickname.setType(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.TYPE)));
            		Email email=new Email();	
            		email.setAddress(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            		email.setContact(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)));
            		email.setLabel(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)));
            		email.setType(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
            		 
            		objContact.setNickname(nickname);
            		objContact.setEmail(email);
            		objContact.setPhone(phone);
            		objContact.setPhoto(photo);
            		 
            		 
            		PreyLogger.i(objContact.toString());
            		array.put(objContact.toJSONObject());
            		 
	                c.moveToNext();
	            }
	        }
		}catch(Exception e){
			 
		}finally{
			if (c!=null){
				 c.close();
			}
		}
 		return array;
	}
}
