package com.prey.activities;

import com.prey.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

public class FormFeedBackActivity  extends PreyActivity {
	
	private static final int SHOW_POPUP = 0;
	private String message = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.message = bundle.getString("alert_message");
		}

 
		showDialog(SHOW_POPUP);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog popup = null;
		switch (id) {

		case SHOW_POPUP:
			AlertDialog.Builder alert = null;
			 

			 
			alert = new AlertDialog.Builder(this);
			alert.setIcon(R.drawable.logo).setTitle(R.string.popup_alert_title).setMessage(this.message)
				.create();
			
			LayoutInflater inflater = this.getLayoutInflater();

				
			alert.setView(inflater.inflate(R.layout.dialog_signin, null));

			
			alert.setPositiveButton("Button 1 Text", new DialogInterface.OnClickListener() {

				      public void onClick(DialogInterface dialog, int id) {

	 
				    	  
				    	  Intent intent = new Intent(Intent.ACTION_VIEW);
				    	  intent.setData(Uri.parse("market://details?id=com.prey"));
				    	  startActivity(intent);
				    	  
				    	  finish();

				    } }); 

			alert.setNeutralButton( "Button 2 Text", new DialogInterface.OnClickListener() {

				      public void onClick(DialogInterface dialog, int id) {

				        //...

				    }}); 

			alert.setNegativeButton("Button 3 Text", new DialogInterface.OnClickListener() {

				      public void onClick(DialogInterface dialog, int id) {

				        //...
				    	  finish();
				    }});
			
			popup=alert.create();
		}
		return popup;
	}
 
 

}
