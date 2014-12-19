/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.PreyUtils;
import com.prey.contacts.ContactAccessor;
import com.prey.contacts.ContactInfo;
import com.prey.exceptions.SMSNotSendException;
import com.prey.sms.SMSSupport;
import com.prey.R;
public class SMSContactActivity extends PreyActivity {

	private static final int PICK_CONTACT_REQUEST = 0;
	ContactAccessor contactAccesor = new ContactAccessor();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		fillScreenInfo(getPreyConfig().getDestinationSmsName(), getPreyConfig().getDestinationSmsNumber(),null);
		
		View.OnClickListener launchContactPicker = new View.OnClickListener() {
			public void onClick(View v) {
				doLaunchContactPicker(getCurrentFocus());
			}
		};
		
		Button change = (Button) findViewById(R.id.sms_btn_change);
		change.setOnClickListener(launchContactPicker);
		
		//ImageView picture = (ImageView) findViewById(R.id.sms_sheriff);
		//picture.setOnClickListener(launchContactPicker);
		
		Button ok = (Button) findViewById(R.id.sms_btn_accept);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
			}
		});
		
	}
	
	@Override
	public void onBackPressed(){
		this.close();
	}
	
	private void close(){
		Intent intent = new Intent(SMSContactActivity.this, PreyConfigurationActivity.class);
		PreyStatus.getInstance().setPreyConfigurationActivityResume(true);
		startActivity(intent);
		finish();
	}

	public void doLaunchContactPicker(View view) {
		startActivityForResult(contactAccesor.getPickContactIntent(), PICK_CONTACT_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		PreyLogger.d("Activity returned");
		if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK)
			loadContactInfo(data.getData());
	}

	private void showContactNowAlert() {
		try{
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(SMSContactActivity.this);
			try{
				alertDialog.setTitle(getText(R.string.hero_chosen));
			}catch(Exception e){
			}
			try{
				alertDialog.setMessage(getString(R.string.notify_your_hero_now,getPreyConfig().getDestinationSmsName()));
			}catch(Exception e){	
			}
			alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int which) {
					String deviceType = PreyUtils.getDeviceType(SMSContactActivity.this).toLowerCase();
            		try {
            			SMSSupport.sendSMS(getPreyConfig().getDestinationSmsNumber(), getString(R.string.hero_notification_message,deviceType));
					} catch (SMSNotSendException e) {
						Toast.makeText(SMSContactActivity.this, R.string.sms_not_sent, Toast.LENGTH_LONG).show();
					}
            	}
			});
 
			alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
            	}
        	});
        	alertDialog.show();
        }catch(Exception e){
        	
        }
		
	}

	private void loadContactInfo(Uri contactUri) {

		/*
		 * We should always run database queries on a background thread. The
		 * database may be locked by some process for a long time. If we locked
		 * up the UI thread while waiting for the query to come back, we might
		 * get an "Application Not Responding" dialog.
		 */
		AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>() {

			@Override
			protected ContactInfo doInBackground(Uri... uris) {
				return contactAccesor.loadContact(getContentResolver(), uris[0]);
			}

			@Override
			protected void onPostExecute(ContactInfo result) {
				bindView(result);
				showContactNowAlert();
			}
		};
		task.execute(contactUri);
	}

	protected void bindView(ContactInfo contactInfo) {
		String contactNumber = contactInfo.getPhoneNumber();
		String contactName = contactInfo.getDisplayName();
		Bitmap contactPhoto = contactInfo.getPicture();

		if (contactNumber != null && PhoneNumberUtils.isWellFormedSmsAddress(contactNumber)) {
			getPreyConfig().saveDestinationSmsNumber(contactNumber);
			getPreyConfig().saveDestinationSmsName(contactName);
			getPreyConfig().saveDestinationSmsPicture(contactPhoto);
			fillScreenInfo(contactName, contactNumber,contactPhoto);
			PreyLogger.d("SMS contact stored: " + contactInfo.getDisplayName() + " - " + contactInfo.getPhoneNumber());
		} 
		else {
			Toast.makeText(SMSContactActivity.this, R.string.preferences_destination_sms_not_valid, Toast.LENGTH_LONG).show();
		}	
	}
	
	private void fillScreenInfo(String name, String number, Bitmap photo){
		if (name == null || name.equals(""))
			name = getString(R.string.no_hero_selected);
		((TextView) findViewById(R.id.sms_contact_text)).setText(name);
		((TextView) findViewById(R.id.sms_contact_number)).setText(PhoneNumberUtils.formatNumber(number));
		Bitmap b = getPreyConfig().getDestinationSmsPicture();
	/*
		if (b!= null)
			((ImageView) findViewById(R.id.sms_sheriff)).setImageBitmap(b);
		else
			((ImageView) findViewById(R.id.sms_sheriff)).setImageResource(R.drawable.sheriff);
			*/
	}

}
