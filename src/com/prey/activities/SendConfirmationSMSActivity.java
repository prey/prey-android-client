/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prey.ContactsAutoCompleteCursorAdapter;
import com.prey.PreyConfig;
import com.prey.R;

public class SendConfirmationSMSActivity extends PreyActivity {

	private static final int SHOW_CCONFIRMATION = 0;
	protected static final int PICK_CONTACT = 0;
	private String mobileNumber = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sms_destination2);
		((EditText) findViewById(R.id.destination_sms_text)).setText(getPreyConfig().getDestinationSms());

		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.destination_sms_text);
		Cursor c = getContentResolver().query(People.CONTENT_URI, PEOPLE_PROJECTION,
				People.DISPLAY_NAME + " IS NOT NULL AND " + People.NUMBER_KEY + " IS NOT NULL", null, People.DEFAULT_SORT_ORDER);
		ContactsAutoCompleteCursorAdapter adapter = new ContactsAutoCompleteCursorAdapter(this, c);
		textView.setAdapter(adapter);
		// you can also prompt the user with a hint
		textView.setHint(getString(R.string.preferences_destination_sms_edittext_hint));

		Button ok = (Button) findViewById(R.id.save_sms_button);
		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				final String destSMS = ((EditText) findViewById(R.id.destination_sms_text)).getText().toString();

				if (PhoneNumberUtils.isWellFormedSmsAddress(destSMS)) {
					getPreyConfig().saveDestinationSms(destSMS);
					Intent preferecences = new Intent(SendConfirmationSMSActivity.this, PreyConfigurationActivity.class);
					startActivity(preferecences);
				} else
					Toast.makeText(SendConfirmationSMSActivity.this, R.string.preferences_destination_sms_not_valid, Toast.LENGTH_LONG).show();

			}
		});

		Button cancel = (Button) findViewById(R.id.cancel_save_sms_button);
		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				Intent preferecences = new Intent(SendConfirmationSMSActivity.this, PreyConfigurationActivity.class);
				startActivity(preferecences);
			}
		});

		/* ********************** */

		/*
		 * Button selectFromContact = (Button)
		 * findViewById(R.id.ButtonSelectFromContacts);
		 * selectFromContact.setOnClickListener(new View.OnClickListener() {
		 * 
		 * public void onClick(View v) { Intent intent = new
		 * Intent(Intent.ACTION_PICK, People.CONTENT_URI);
		 * startActivityForResult(intent, PICK_CONTACT); } });
		 */

	}

	/*
	 * @Override public void onActivityResult(int reqCode, int resultCode,
	 * Intent data) { super.onActivityResult(reqCode, resultCode, data);
	 * 
	 * switch (reqCode) { case (PICK_CONTACT) : if (resultCode ==
	 * Activity.RESULT_OK) { Uri contactData = data.getData(); Cursor c =
	 * managedQuery(contactData, null, null, null, null); if (c.moveToFirst()) {
	 * String mobilePhone = c.getString(c.getColumnIndexOrThrow(People.NUMBER));
	 * ((EditText)
	 * findViewById(R.id.destination_sms_text)).setText(mobilePhone); } } break;
	 * } }
	 * 
	 * @Override protected Dialog onCreateDialog(int id) {
	 * 
	 * Dialog popup = null; switch (id) {
	 * 
	 * case SHOW_CCONFIRMATION: popup = new
	 * AlertDialog.Builder(SendConfirmationSMSActivity
	 * .this).setIcon(R.drawable.logo
	 * ).setTitle(R.string.popup_alert_title).setMessage(this.mobileNumber)
	 * .setCancelable(true).create();
	 * 
	 * popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
	 * public void onDismiss(DialogInterface dialog) { finish(); } }); } return
	 * popup; }
	 */

	private static final String[] PEOPLE_PROJECTION = new String[] { Contacts.People._ID, Contacts.People.PRIMARY_PHONE_ID, Contacts.People.TYPE,
			Contacts.People.NUMBER, Contacts.People.LABEL, Contacts.People.DISPLAY_NAME, };

}
